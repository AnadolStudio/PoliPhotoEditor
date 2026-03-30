from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_db
from app.dependencies import get_current_user
from app.models import User
from app.schemas import (
    LoginRequest,
    LogoutRequest,
    MessageResponse,
    RefreshRequest,
    RegisterRequest,
    TokenResponse,
)
from app.auth import service as auth_service
from app.auth.utils import verify_password

router = APIRouter()


@router.post("/register", response_model=TokenResponse, status_code=status.HTTP_201_CREATED)
async def register(payload: RegisterRequest, db: AsyncSession = Depends(get_db)):
    existing = await auth_service.get_user_by_email(db, payload.email)
    if existing:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Email already registered",
        )

    user = await auth_service.create_user(
        db, email=payload.email, password=payload.password, username=payload.username
    )
    access_token, raw_refresh = await auth_service.issue_token_pair(db, user)
    return TokenResponse(access_token=access_token, refresh_token=raw_refresh)


@router.post("/login", response_model=TokenResponse)
async def login(payload: LoginRequest, db: AsyncSession = Depends(get_db)):
    user = await auth_service.get_user_by_email(db, payload.email)
    if not user or not verify_password(payload.password, user.password_hash):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid email or password",
        )

    access_token, raw_refresh = await auth_service.issue_token_pair(db, user)
    return TokenResponse(access_token=access_token, refresh_token=raw_refresh)


@router.post("/refresh", response_model=TokenResponse)
async def refresh(payload: RefreshRequest, db: AsyncSession = Depends(get_db)):
    result = await auth_service.find_refresh_token_by_raw(db, payload.refresh_token)
    if result is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or expired refresh token",
        )

    old_rt, user = result
    await auth_service.revoke_refresh_token(db, old_rt)

    access_token, new_raw_refresh = await auth_service.issue_token_pair(db, user)
    return TokenResponse(access_token=access_token, refresh_token=new_raw_refresh)


@router.post("/logout", response_model=MessageResponse)
async def logout(
    payload: LogoutRequest,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    rt = await auth_service.find_valid_refresh_token(
        db, current_user.id, payload.refresh_token
    )
    if rt is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Refresh token not found or already revoked",
        )

    await auth_service.revoke_refresh_token(db, rt)
    return MessageResponse(message="Successfully logged out")
