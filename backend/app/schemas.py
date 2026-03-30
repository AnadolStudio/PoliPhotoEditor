from datetime import datetime
from typing import Optional
from pydantic import BaseModel, EmailStr, field_validator


# ── Auth schemas ──────────────────────────────────────────────────────────────

class RegisterRequest(BaseModel):
    email: EmailStr
    password: str
    username: Optional[str] = None

    @field_validator("password")
    @classmethod
    def password_min_length(cls, v: str) -> str:
        if len(v) < 8:
            raise ValueError("Password must be at least 8 characters long")
        return v


class LoginRequest(BaseModel):
    email: EmailStr
    password: str


class TokenResponse(BaseModel):
    access_token: str
    refresh_token: str
    token_type: str = "bearer"


class RefreshRequest(BaseModel):
    refresh_token: str


class LogoutRequest(BaseModel):
    refresh_token: str


class MessageResponse(BaseModel):
    message: str


# ── User schemas ──────────────────────────────────────────────────────────────

class UserResponse(BaseModel):
    id: int
    email: str
    username: Optional[str]
    role: str
    created_at: datetime

    model_config = {"from_attributes": True}


class UpdateUserRequest(BaseModel):
    username: Optional[str] = None
    email: Optional[EmailStr] = None


# ── Features schemas ───────────────────────────────────────────────────────────

class FeatureAccessResponse(BaseModel):
    features: dict[str, bool]


class FilterItem(BaseModel):
    id: int
    name: str
    group: str
    available: bool


class FiltersResponse(BaseModel):
    filters: list[FilterItem]
