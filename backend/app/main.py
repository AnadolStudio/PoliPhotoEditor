from contextlib import asynccontextmanager
from typing import Optional

from dotenv import load_dotenv
load_dotenv()

from fastapi import APIRouter, Depends, HTTPException, status
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_db, init_db
from app.dependencies import get_current_user, get_current_user_optional
from app.feature_gates import get_feature_access, get_filters_with_access
from app.models import User
from app.schemas import (
    UpdateUserRequest, UserResponse,
    FeatureAccessResponse, FiltersResponse, FilterItem,
)
from app.auth.router import router as auth_router


@asynccontextmanager
async def lifespan(app: FastAPI):
    await init_db()
    yield


app = FastAPI(title="PoliPhotoEditor API", version="1.0.0", lifespan=lifespan)

# CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ── Auth routes ────────────────────────────────────────────────────────────────
app.include_router(auth_router, prefix="/auth", tags=["auth"])

# ── User routes ────────────────────────────────────────────────────────────────
user_router = APIRouter(prefix="/user", tags=["user"])


@user_router.get("/me", response_model=UserResponse)
async def get_me(current_user: User = Depends(get_current_user)):
    return current_user


@user_router.put("/me", response_model=UserResponse)
async def update_me(
    payload: UpdateUserRequest,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    if payload.email is not None:
        from app.auth.service import get_user_by_email
        existing = await get_user_by_email(db, str(payload.email))
        if existing and existing.id != current_user.id:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Email already in use",
            )
        current_user.email = str(payload.email)

    if payload.username is not None:
        current_user.username = payload.username

    await db.flush()
    await db.refresh(current_user)
    return current_user


app.include_router(user_router)

# ── Features routes (optional auth — accessible by guests too) ─────────────────
features_router = APIRouter(prefix="/features", tags=["features"])


@features_router.get("/access", response_model=FeatureAccessResponse)
async def get_access(
    current_user: Optional[User] = Depends(get_current_user_optional),
):
    """
    Returns a map of {feature: available} for the caller.
    Works without authentication — guests receive the limited feature set.
    Invalid tokens are treated as guest (no 401).
    """
    is_authenticated = current_user is not None
    return FeatureAccessResponse(features=get_feature_access(is_authenticated))


@features_router.get("/filters", response_model=FiltersResponse)
async def get_filters(
    current_user: Optional[User] = Depends(get_current_user_optional),
):
    """
    Returns the full filter list with an `available` flag per filter.
    Guests get filters 0-19; Person gets all 95 filters.
    """
    is_authenticated = current_user is not None
    filters = [FilterItem(**f) for f in get_filters_with_access(is_authenticated)]
    return FiltersResponse(filters=filters)


app.include_router(features_router)
