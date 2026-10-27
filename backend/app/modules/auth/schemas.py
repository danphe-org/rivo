from typing import Optional
from pydantic import BaseModel, ConfigDict, EmailStr, Field

from app.modules.users.schemas import UserResponseDTO


class LoginDTO(BaseModel):
    email: EmailStr = Field(...)
    password: str = Field(..., min_length=1)

    model_config = ConfigDict(str_strip_whitespace=True)


class RefreshTokenDTO(BaseModel):
    refresh_token: Optional[str] = Field(default=None)

    model_config = ConfigDict(str_strip_whitespace=True)


class TokenResponseDTO(BaseModel):
    access_token: str
    refresh_token: Optional[str] = None
    token_type: str = "Bearer"


class LoginResponseDTO(UserResponseDTO):
    access_token: Optional[str] = None
    refresh_token: Optional[str] = None
    token_type: str = "Bearer"
