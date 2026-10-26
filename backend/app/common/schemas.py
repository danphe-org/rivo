"""Standard REST envelopes and common schemas matching bazar-backend.

Mirrors:
- com.danphe.bazar.common.library.base.rest.ResponseTemplate<T>
- com.danphe.bazar.common.library.base.rest.RestResponse
- com.danphe.bazar.common.library.base.rest.constant.ApiConstants
"""
import uuid
from datetime import datetime
from typing import Any, Generic, Optional, TypeVar
from pydantic import BaseModel, Field

from app.common.constants import ApiConstants
from app.common.pagination import AbstractFilter, Page
from app.common.utils import DateUtils

T = TypeVar("T")


class ResponseTemplate(BaseModel, Generic[T]):
    """Standard API response envelope matching bazar-backend's ResponseTemplate<T>."""

    code: str = Field(default=ApiConstants.SUCCESS_CODE, description="Status code string ('0' for success, HTTP/error code for errors)")
    message: str = Field(default=ApiConstants.SUCCESS, description="Status or error message")
    data: Optional[T] = Field(default=None, description="Payload data or error detail")
    time: str = Field(default_factory=DateUtils.now, description="Timestamp string")


class ErrorResponse(BaseModel):
    """Explicit error response envelope."""

    code: str = Field(..., description="Error code string")
    message: str = Field(..., description="Error message")
    data: Optional[Any] = Field(default=None, description="Optional extra error details or trace")
    time: str = Field(default_factory=DateUtils.now, description="Timestamp string")


class BaseAuditSchema(BaseModel):
    """Pydantic schema mixin carrying project-wide audit fields for response DTOs."""

    id: Optional[uuid.UUID] = Field(default=None, description="Unique identifier (UUID)")
    created_on: Optional[datetime] = Field(default=None, description="Created timestamp")
    modified_on: Optional[datetime] = Field(default=None, description="Last modified timestamp")
    created_by: Optional[str] = Field(default=None, description="Created by user id/name")
    modified_by: Optional[str] = Field(default=None, description="Last modified by user id/name")
    active: bool = Field(default=True, description="Active status")
    deleted: bool = Field(default=False, description="Soft-delete status")

    model_config = {"from_attributes": True}


class RestResponse:
    """Helper factory matching bazar-backend RestResponse."""

    SUCCESS: str = ApiConstants.SUCCESS
    SUCCESS_CODE: str = ApiConstants.SUCCESS_CODE
    ERROR_CODE: str = ApiConstants.ERROR_CODE

    @classmethod
    def of(cls, data: Optional[T] = None, message: str = ApiConstants.SUCCESS, code: str = ApiConstants.SUCCESS_CODE) -> ResponseTemplate[T]:
        """Wrap payload in ResponseTemplate."""
        return ResponseTemplate[T](
            code=code,
            message=message,
            data=data,
            time=DateUtils.now(),
        )

    @classmethod
    def ok(cls, data: Optional[T] = None, message: str = ApiConstants.SUCCESS) -> ResponseTemplate[T]:
        """Convenience method returning successful response."""
        return cls.of(data=data, message=message, code=cls.SUCCESS_CODE)

    @classmethod
    def error(
        cls,
        message: str,
        code: str = "400",
        data: Optional[Any] = None,
    ) -> ResponseTemplate[Any]:
        """Convenience method returning error response envelope."""
        return ResponseTemplate[Any](
            code=code,
            message=message,
            data=data,
            time=DateUtils.now(),
        )


__all__ = [
    "ApiConstants",
    "ResponseTemplate",
    "ErrorResponse",
    "BaseAuditSchema",
    "RestResponse",
    "Page",
    "AbstractFilter",
]
