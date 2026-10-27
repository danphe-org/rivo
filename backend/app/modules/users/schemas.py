import uuid
from typing import List, Optional
from pydantic import BaseModel, ConfigDict, EmailStr, Field, field_validator

from app.common.pagination import AbstractFilter
from app.common.schemas import BaseAuditSchema


class UserCreateDTO(BaseModel):
    name: str = Field(..., min_length=2, max_length=100)
    email: EmailStr = Field(...)
    password: Optional[str] = Field(default=None, max_length=128)
    phone: Optional[str] = Field(default=None, max_length=30)
    role: str = Field(default="USER", max_length=50)

    model_config = ConfigDict(str_strip_whitespace=True)

    @field_validator("name")
    @classmethod
    def validate_name_not_blank(cls, v: str) -> str:
        if not v or not v.strip():
            raise ValueError("Name cannot be empty or whitespace only")
        return v.strip()


class UserRegisterDTO(BaseModel):
    name: str = Field(..., min_length=2, max_length=100)
    email: EmailStr = Field(...)
    password: Optional[str] = Field(default=None, max_length=128)
    phone: Optional[str] = Field(default=None, max_length=30)

    model_config = ConfigDict(str_strip_whitespace=True)

    @field_validator("name")
    @classmethod
    def validate_name_not_blank(cls, v: str) -> str:
        if not v or not v.strip():
            raise ValueError("Name cannot be empty or whitespace only")
        return v.strip()


class UserUpdateDTO(BaseModel):
    name: Optional[str] = Field(default=None, min_length=2, max_length=100)
    email: Optional[EmailStr] = Field(default=None)
    password: Optional[str] = Field(default=None, max_length=128)
    phone: Optional[str] = Field(default=None, max_length=30)
    role: Optional[str] = Field(default=None, max_length=50)
    active: Optional[bool] = Field(default=None)

    model_config = ConfigDict(str_strip_whitespace=True)

    @field_validator("name")
    @classmethod
    def validate_name_not_blank(cls, v: Optional[str]) -> Optional[str]:
        if v is not None:
            if not v or not v.strip():
                raise ValueError("Name cannot be empty or whitespace only")
            return v.strip()
        return v


class UserRoleResponseDTO(BaseAuditSchema):
    id: uuid.UUID = Field(...)
    user_id: uuid.UUID = Field(...)
    role: str = Field(...)

    model_config = ConfigDict(from_attributes=True)


class UserResponseDTO(BaseAuditSchema):
    id: uuid.UUID = Field(...)
    name: str = Field(...)
    email: str = Field(...)
    phone: Optional[str] = Field(default=None)
    role: str = Field(...)
    roles: Optional[List[UserRoleResponseDTO]] = Field(default=None)

    model_config = ConfigDict(from_attributes=True)


class UserFilter(AbstractFilter):
    role: Optional[str] = Field(default=None)
    email: Optional[str] = Field(default=None)
