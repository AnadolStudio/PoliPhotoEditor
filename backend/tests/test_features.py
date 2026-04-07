"""Tests for GET /features/access and GET /features/filters endpoints."""
import pytest
import pytest_asyncio
from httpx import AsyncClient, ASGITransport
from sqlalchemy.ext.asyncio import create_async_engine, async_sessionmaker

from app.main import app
from app.database import get_db, Base

# ── In-memory SQLite for tests ─────────────────────────────────────────────────
TEST_DB_URL = "sqlite+aiosqlite:///:memory:"


@pytest_asyncio.fixture
async def db_session():
    engine = create_async_engine(TEST_DB_URL, future=True)
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    session_factory = async_sessionmaker(engine, expire_on_commit=False)
    async with session_factory() as session:
        yield session
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.drop_all)
    await engine.dispose()


@pytest_asyncio.fixture
async def client(db_session):
    async def override_get_db():
        yield db_session

    app.dependency_overrides[get_db] = override_get_db
    async with AsyncClient(
        transport=ASGITransport(app=app), base_url="http://test"
    ) as ac:
        yield ac
    app.dependency_overrides.clear()


async def _register_and_login(client: AsyncClient) -> str:
    """Helper: register a user and return access token."""
    await client.post("/auth/register", json={
        "email": "person@example.com",
        "password": "password123",
        "username": "person",
    })
    resp = await client.post("/auth/login", json={
        "email": "person@example.com",
        "password": "password123",
    })
    return resp.json()["access_token"]


# ── /features/access ──────────────────────────────────────────────────────────

@pytest.mark.asyncio
async def test_access_guest_no_token(client):
    """Guest (no token) gets limited feature set."""
    resp = await client.get("/features/access")
    assert resp.status_code == 200
    features = resp.json()["features"]
    # Guest-allowed features must be True
    assert features["filters_basic"] is True
    assert features["crop_rotate_flip"] is True
    assert features["improve_basic"] is True
    assert features["text_basic"] is True
    assert features["stickers_basic"] is True
    assert features["frames_basic"] is True
    assert features["collage_basic"] is True
    assert features["forms_basic"] is True


@pytest.mark.asyncio
async def test_access_guest_restricted_features(client):
    """Guest cannot access person-only features."""
    resp = await client.get("/features/access")
    features = resp.json()["features"]
    assert features["filters_all"] is False
    assert features["curves"] is False
    assert features["effects"] is False
    assert features["fonts_extended"] is False
    assert features["stickers_all"] is False
    assert features["frames_all"] is False
    assert features["collage_all"] is False
    assert features["forms_all"] is False
    assert features["background_removal"] is False
    assert features["body_editor"] is False
    assert features["export_no_watermark"] is False


@pytest.mark.asyncio
async def test_access_person_all_features(client):
    """Authenticated person gets all features enabled."""
    token = await _register_and_login(client)
    resp = await client.get(
        "/features/access",
        headers={"Authorization": f"Bearer {token}"},
    )
    assert resp.status_code == 200
    features = resp.json()["features"]
    assert all(v is True for v in features.values()), (
        f"Person should have all features True, got: "
        f"{[k for k, v in features.items() if not v]}"
    )


@pytest.mark.asyncio
async def test_access_invalid_token_treated_as_guest(client):
    """Invalid token → no 401, treated as guest."""
    resp = await client.get(
        "/features/access",
        headers={"Authorization": "Bearer invalidtoken.abc.xyz"},
    )
    assert resp.status_code == 200
    features = resp.json()["features"]
    # Should behave like guest
    assert features["background_removal"] is False
    assert features["filters_basic"] is True


@pytest.mark.asyncio
async def test_access_contains_all_expected_keys(client):
    """Response must contain all 19 feature keys."""
    resp = await client.get("/features/access")
    features = resp.json()["features"]
    expected_keys = {
        "filters_basic", "filters_all", "crop_rotate_flip", "improve_basic",
        "curves", "effects", "text_basic", "fonts_extended",
        "stickers_basic", "stickers_all", "frames_basic", "frames_all",
        "collage_basic", "collage_all", "forms_basic", "forms_all",
        "background_removal", "body_editor", "export_no_watermark",
    }
    assert set(features.keys()) == expected_keys


# ── /features/filters ─────────────────────────────────────────────────────────

@pytest.mark.asyncio
async def test_filters_guest_first_20_available(client):
    """Guest: filters 0-19 are available."""
    resp = await client.get("/features/filters")
    assert resp.status_code == 200
    filters = {f["id"]: f for f in resp.json()["filters"]}
    for i in range(20):
        assert filters[i]["available"] is True, f"Filter {i} should be available to guest"


@pytest.mark.asyncio
async def test_filters_guest_rest_unavailable(client):
    """Guest: filters 20+ are not available."""
    resp = await client.get("/features/filters")
    filters = {f["id"]: f for f in resp.json()["filters"]}
    for i in range(20, 95):
        assert filters[i]["available"] is False, f"Filter {i} should not be available to guest"


@pytest.mark.asyncio
async def test_filters_person_all_available(client):
    """Person: all 95 filters are available."""
    token = await _register_and_login(client)
    resp = await client.get(
        "/features/filters",
        headers={"Authorization": f"Bearer {token}"},
    )
    assert resp.status_code == 200
    filters = resp.json()["filters"]
    assert len(filters) == 95
    assert all(f["available"] is True for f in filters), (
        f"Person should have all filters available"
    )


@pytest.mark.asyncio
async def test_filters_count(client):
    """Total filter count must be 95 (Original + all groups)."""
    resp = await client.get("/features/filters")
    assert len(resp.json()["filters"]) == 95


@pytest.mark.asyncio
async def test_filters_structure(client):
    """Each filter item has required fields."""
    resp = await client.get("/features/filters")
    for f in resp.json()["filters"]:
        assert "id" in f
        assert "name" in f
        assert "group" in f
        assert "available" in f


@pytest.mark.asyncio
async def test_filters_invalid_token_treated_as_guest(client):
    """Invalid token on /features/filters → treated as guest, no 401."""
    resp = await client.get(
        "/features/filters",
        headers={"Authorization": "Bearer bad.token.here"},
    )
    assert resp.status_code == 200
    filters = {f["id"]: f for f in resp.json()["filters"]}
    assert filters[0]["available"] is True
    assert filters[50]["available"] is False
