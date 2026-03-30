from datetime import datetime, timezone
from typing import Optional, Tuple

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models import RefreshToken, User
from app.auth.utils import (
    create_access_token,
    generate_refresh_token_value,
    hash_password,
    hash_token,
    refresh_token_expires_at,
    verify_password,
    verify_token_hash,
)


async def get_user_by_email(db: AsyncSession, email: str) -> Optional[User]:
    result = await db.execute(select(User).where(User.email == email))
    return result.scalars().first()


async def get_user_by_id(db: AsyncSession, user_id: int) -> Optional[User]:
    result = await db.execute(select(User).where(User.id == user_id))
    return result.scalars().first()


async def create_user(
    db: AsyncSession, email: str, password: str, username: Optional[str] = None
) -> User:
    user = User(
        email=email,
        password_hash=hash_password(password),
        username=username,
        role="person",
    )
    db.add(user)
    await db.flush()  # populate user.id without committing
    return user


async def issue_token_pair(
    db: AsyncSession, user: User
) -> Tuple[str, str]:
    """Create a new access token + refresh token, persist the refresh token hash."""
    access_token = create_access_token(user.id, user.role)

    raw_refresh = generate_refresh_token_value()
    rt = RefreshToken(
        user_id=user.id,
        token_hash=hash_token(raw_refresh),
        expires_at=refresh_token_expires_at(),
        revoked=False,
    )
    db.add(rt)
    await db.flush()

    return access_token, raw_refresh


async def find_valid_refresh_token(
    db: AsyncSession, user_id: int, raw_token: str
) -> Optional[RefreshToken]:
    """Return the matching un-revoked, un-expired RefreshToken row, or None."""
    result = await db.execute(
        select(RefreshToken).where(
            RefreshToken.user_id == user_id,
            RefreshToken.revoked == False,  # noqa: E712
            RefreshToken.expires_at > datetime.now(timezone.utc),
        )
    )
    candidates = result.scalars().all()
    for rt in candidates:
        if verify_token_hash(raw_token, rt.token_hash):
            return rt
    return None


async def find_refresh_token_by_raw(
    db: AsyncSession, raw_token: str
) -> Optional[Tuple[RefreshToken, User]]:
    """
    Scan all non-revoked, non-expired refresh tokens and find one whose hash
    matches raw_token. Returns (RefreshToken, User) or None.
    """
    result = await db.execute(
        select(RefreshToken, User)
        .join(User, RefreshToken.user_id == User.id)
        .where(
            RefreshToken.revoked == False,  # noqa: E712
            RefreshToken.expires_at > datetime.now(timezone.utc),
        )
    )
    rows = result.all()
    for rt, user in rows:
        if verify_token_hash(raw_token, rt.token_hash):
            return rt, user
    return None


async def revoke_refresh_token(db: AsyncSession, rt: RefreshToken) -> None:
    rt.revoked = True
    await db.flush()
