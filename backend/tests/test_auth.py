"""
Comprehensive auth tests using an isolated in-memory SQLite database.
"""
from __future__ import annotations

from typing import Optional
import pytest
import pytest_asyncio
from httpx import AsyncClient, ASGITransport
from sqlalchemy.ext.asyncio import create_async_engine, async_sessionmaker, AsyncSession

# ── App imports ───────────────────────────────────────────────────────────────
from app.main import app
from app.database import Base, get_db

# ── In-memory test DB setup ───────────────────────────────────────────────────
TEST_DATABASE_URL = "sqlite+aiosqlite:///:memory:"

test_engine = create_async_engine(TEST_DATABASE_URL, connect_args={"check_same_thread": False})
TestSessionLocal = async_sessionmaker(
    bind=test_engine,
    class_=AsyncSession,
    expire_on_commit=False,
    autocommit=False,
    autoflush=False,
)


async def override_get_db():
    async with TestSessionLocal() as session:
        try:
            yield session
            await session.commit()
        except Exception:
            await session.rollback()
            raise
        finally:
            await session.close()


# Apply dependency override once at module level
app.dependency_overrides[get_db] = override_get_db


@pytest_asyncio.fixture(scope="function", autouse=True)
async def setup_database():
    """Create all tables before each test and drop them after."""
    async with test_engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    yield
    async with test_engine.begin() as conn:
        await conn.run_sync(Base.metadata.drop_all)


@pytest_asyncio.fixture
async def client():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        yield ac


# ── Helper ────────────────────────────────────────────────────────────────────

async def register_user(
    client: AsyncClient,
    email: str = "user@example.com",
    password: str = "securepass",
    username: Optional[str] = "testuser",
):
    payload = {"email": email, "password": password}
    if username is not None:
        payload["username"] = username
    return await client.post("/auth/register", json=payload)


# ═════════════════════════════════════════════════════════════════════════════
# 1. Register success
# ═════════════════════════════════════════════════════════════════════════════
@pytest.mark.asyncio
async def test_register_success(client: AsyncClient):
    resp = await register_user(client)
    assert resp.status_code == 201
    data = resp.json()
    assert "access_token" in data
    assert "refresh_token" in data
    assert data["token_type"] == "bearer"


# ═════════════════════════════════════════════════════════════════════════════
# 2. Register duplicate email fails (400)
# ═════════════════════════════════════════════════════════════════════════════
@pytest.mark.asyncio
async def test_register_duplicate_email(client: AsyncClient):
    await register_user(client)
    resp = await register_user(client)
    assert resp.status_code == 400
    assert "already registered" in resp.json()["detail"].lower()


# ═════════════════════════════════════════════════════════════════════════════
# 3. Register short password fails (422)
# ═════════════════════════════════════════════════════════════════════════════
@pytest.mark.asyncio
async def test_register_short_password(client: AsyncClient):
    resp = await register_user(client, password="short")
    assert resp.status_code == 422


# ═════════════════════════════════════════════════════════════════════════════
# 4. Login success → returns tokens
# ═════════════════════════════════════════════════════════════════════════════
@pytest.mark.asyncio
async def test_login_success(client: AsyncClient):
    await register_user(client, email="login@example.com", password="goodpassword")
    resp = await client.post(
        "/auth/login", json={"email": "login@example.com", "password": "goodpassword"}
    )
    assert resp.status_code == 200
    data = resp.json()
    assert "access_token" in data
    assert "refresh_token" in data


# ═════════════════════════════════════════════════════════════════════════════
# 5. Login wrong password fails (401)
# ═════════════════════════════════════════════════════════════════════════════
@pytest.mark.asyncio
async def test_login_wrong_password(client: AsyncClient):
    await register_user(client, email="wp@example.com", password="correctpass")
    resp = await client.post(
        "/auth/login", json={"email": "wp@example.com", "password": "wrongpassword"}
    )
    assert resp.status_code == 401


# ═════════════════════════════════════════════════════════════════════════════
# 6. Login unknown email fails (401)
# ═════════════════════════════════════════════════════════════════════════════
@pytest.mark.asyncio
async def test_login_unknown_email(client: AsyncClient):
    resp = await client.post(
        "/auth/login", json={"email": "nobody@example.com", "password": "somepassword"}
    )
    assert resp.status_code == 401


# ═════════════════════════════════════════════════════════════════════════════
# 7. Access /user/me with valid token → returns user
# ═════════════════════════════════════════════════════════════════════════════
@pytest.mark.asyncio
async def test_get_me_valid_token(client: AsyncClient):
    reg = await register_user(client, email="me@example.com")
    access_token = reg.json()["access_token"]

    resp = await client.get(
        "/user/me", headers={"Authorization": f"Bearer {access_token}"}
    )
    assert resp.status_code == 200
    data = resp.json()
    assert data["email"] == "me@example.com"
    assert data["role"] == "person"
    assert "id" in data
    assert "created_at" in data


# ═════════════════════════════════════════════════════════════════════════════
# 8. Access /user/me with invalid token → 401
# ═════════════════════════════════════════════════════════════════════════════
@pytest.mark.asyncio
async def test_get_me_invalid_token(client: AsyncClient):
    resp = await client.get(
        "/user/me", headers={"Authorization": "Bearer this.is.not.valid"}
    )
    assert resp.status_code == 401


# ═════════════════════════════════════════════════════════════════════════════
# 9. Access /user/me with no token → 401
# ═════════════════════════════════════════════════════════════════════════════
@pytest.mark.asyncio
async def test_get_me_no_token(client: AsyncClient):
    resp = await client.get("/user/me")
    assert resp.status_code == 403


# ═════════════════════════════════════════════════════════════════════════════
# 10. Refresh token → returns new access token, old refresh revoked
# ═════════════════════════════════════════════════════════════════════════════
@pytest.mark.asyncio
async def test_refresh_token_success(client: AsyncClient):
    reg = await register_user(client, email="refresh@example.com")
    old_refresh = reg.json()["refresh_token"]

    resp = await client.post("/auth/refresh", json={"refresh_token": old_refresh})
    assert resp.status_code == 200
    data = resp.json()
    assert "access_token" in data
    assert "refresh_token" in data
    # New refresh token must be different
    assert data["refresh_token"] != old_refresh


# ═════════════════════════════════════════════════════════════════════════════
# 11. Use old refresh token after refresh → 401
# ═════════════════════════════════════════════════════════════════════════════
@pytest.mark.asyncio
async def test_old_refresh_token_rejected(client: AsyncClient):
    reg = await register_user(client, email="oldrt@example.com")
    old_refresh = reg.json()["refresh_token"]

    # Use it once
    await client.post("/auth/refresh", json={"refresh_token": old_refresh})

    # Try again with old token
    resp = await client.post("/auth/refresh", json={"refresh_token": old_refresh})
    assert resp.status_code == 401


# ═════════════════════════════════════════════════════════════════════════════
# 12. Logout → refresh token revoked
# ═════════════════════════════════════════════════════════════════════════════
@pytest.mark.asyncio
async def test_logout_success(client: AsyncClient):
    reg = await register_user(client, email="logout@example.com")
    tokens = reg.json()
    access_token = tokens["access_token"]
    refresh_token = tokens["refresh_token"]

    resp = await client.post(
        "/auth/logout",
        json={"refresh_token": refresh_token},
        headers={"Authorization": f"Bearer {access_token}"},
    )
    assert resp.status_code == 200
    assert "logged out" in resp.json()["message"].lower()


# ═════════════════════════════════════════════════════════════════════════════
# 13. Use refresh token after logout → 401
# ═════════════════════════════════════════════════════════════════════════════
@pytest.mark.asyncio
async def test_refresh_after_logout_fails(client: AsyncClient):
    reg = await register_user(client, email="afterlogout@example.com")
    tokens = reg.json()
    access_token = tokens["access_token"]
    refresh_token = tokens["refresh_token"]

    # Logout
    await client.post(
        "/auth/logout",
        json={"refresh_token": refresh_token},
        headers={"Authorization": f"Bearer {access_token}"},
    )

    # Try to refresh with the revoked token
    resp = await client.post("/auth/refresh", json={"refresh_token": refresh_token})
    assert resp.status_code == 401


# ═════════════════════════════════════════════════════════════════════════════
# 14. Update user profile (PUT /user/me)
# ═════════════════════════════════════════════════════════════════════════════
@pytest.mark.asyncio
async def test_update_user_profile(client: AsyncClient):
    reg = await register_user(client, email="update@example.com", username="original")
    access_token = reg.json()["access_token"]

    # Update username
    resp = await client.put(
        "/user/me",
        json={"username": "updated_name"},
        headers={"Authorization": f"Bearer {access_token}"},
    )
    assert resp.status_code == 200
    assert resp.json()["username"] == "updated_name"

    # Update email
    resp2 = await client.put(
        "/user/me",
        json={"email": "newemail@example.com"},
        headers={"Authorization": f"Bearer {access_token}"},
    )
    assert resp2.status_code == 200
    assert resp2.json()["email"] == "newemail@example.com"


# ═════════════════════════════════════════════════════════════════════════════
# 15. Registered user has role 'person'
# ═════════════════════════════════════════════════════════════════════════════
@pytest.mark.asyncio
async def test_register_role_is_person(client: AsyncClient):
    resp = await register_user(client, email="rolecheck@example.com")
    assert resp.status_code == 201
    access_token = resp.json()["access_token"]

    me = await client.get(
        "/user/me", headers={"Authorization": f"Bearer {access_token}"}
    )
    assert me.status_code == 200
    assert me.json()["role"] == "person"


# ═════════════════════════════════════════════════════════════════════════════
# 16. /features/access — person gets all features True
# ═════════════════════════════════════════════════════════════════════════════
@pytest.mark.asyncio
async def test_features_access_person_all_true(client: AsyncClient):
    reg = await register_user(client, email="features@example.com")
    access_token = reg.json()["access_token"]

    resp = await client.get(
        "/features/access", headers={"Authorization": f"Bearer {access_token}"}
    )
    assert resp.status_code == 200
    features = resp.json()["features"]
    assert all(v is True for v in features.values()), (
        f"Person should have all features, missing: {[k for k,v in features.items() if not v]}"
    )


# ═════════════════════════════════════════════════════════════════════════════
# 17. /features/access — guest gets limited feature set
# ═════════════════════════════════════════════════════════════════════════════
@pytest.mark.asyncio
async def test_features_access_guest_limited(client: AsyncClient):
    resp = await client.get("/features/access")
    assert resp.status_code == 200
    features = resp.json()["features"]
    assert features["background_removal"] is False
    assert features["body_editor"] is False
    assert features["filters_basic"] is True
    assert features["crop_rotate_flip"] is True


# ═════════════════════════════════════════════════════════════════════════════
# 18. /features/filters — person gets all 95 filters available
# ═════════════════════════════════════════════════════════════════════════════
@pytest.mark.asyncio
async def test_features_filters_person_all_available(client: AsyncClient):
    reg = await register_user(client, email="filterson@example.com")
    access_token = reg.json()["access_token"]

    resp = await client.get(
        "/features/filters",
        headers={"Authorization": f"Bearer {access_token}"},
    )
    assert resp.status_code == 200
    filters = resp.json()["filters"]
    assert len(filters) == 95
    assert all(f["available"] is True for f in filters)


# ═════════════════════════════════════════════════════════════════════════════
# 19. /features/filters — guest gets only first 20 filters
# ═════════════════════════════════════════════════════════════════════════════
@pytest.mark.asyncio
async def test_features_filters_guest_limited(client: AsyncClient):
    resp = await client.get("/features/filters")
    assert resp.status_code == 200
    filters = {f["id"]: f for f in resp.json()["filters"]}
    assert filters[0]["available"] is True
    assert filters[19]["available"] is True
    assert filters[20]["available"] is False
    assert filters[94]["available"] is False


# ═════════════════════════════════════════════════════════════════════════════
# 20. /features/* — accessible without auth (no 401 for guests)
# ═════════════════════════════════════════════════════════════════════════════
@pytest.mark.asyncio
async def test_features_accessible_without_auth(client: AsyncClient):
    resp1 = await client.get("/features/access")
    assert resp1.status_code == 200

    resp2 = await client.get("/features/filters")
    assert resp2.status_code == 200
