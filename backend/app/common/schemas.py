import uuid
from datetime import datetime
from typing import Any, Generic, Optional, TypeVar
from pydantic import BaseModel, Field

from app.common.constants import ApiConstants
from app.common.pagination import AbstractFilter, Page
from app.common.utils import DateUtils

T = TypeVar("T")


class ResponseTemplate(BaseModel, Generic[T]):
    code: str = Field(default=ApiConstants.SUCCESS_CODE)
    message: str = Field(default=ApiConstants.SUCCESS)
    data: Optional[T] = Field(default=None)
    time: str = Field(default_factory=DateUtils.now)


class ErrorResponse(BaseModel):
    code: str = Field(...)
    message: str = Field(...)
    data: Optional[Any] = Field(default=None)
    time: str = Field(default_factory=DateUtils.now)


class BaseAuditSchema(BaseModel):
    id: Optional[uuid.UUID] = Field(default=None)
    created_on: Optional[datetime] = Field(default=None)
    modified_on: Optional[datetime] = Field(default=None)
    created_by: Optional[str] = Field(default=None)
    modified_by: Optional[str] = Field(default=None)
    updated_on: Optional[datetime] = Field(default=None)
    updated_by: Optional[str] = Field(default=None)
    active: bool = Field(default=True)
    deleted: bool = Field(default=False)
    remark: Optional[str] = Field(default=None)
    version: Optional[int] = Field(default=1)

    model_config = {"from_attributes": True}


class RestResponse:
    SUCCESS: str = ApiConstants.SUCCESS
    SUCCESS_CODE: str = ApiConstants.SUCCESS_CODE
    ERROR_CODE: str = ApiConstants.ERROR_CODE

    @classmethod
    def of(cls, data: Optional[T] = None, message: str = ApiConstants.SUCCESS, code: str = ApiConstants.SUCCESS_CODE) -> ResponseTemplate[T]:
        return ResponseTemplate[T](
            code=code,
            message=message,
            data=data,
            time=DateUtils.now(),
        )

    @classmethod
    def ok(cls, data: Optional[T] = None, message: str = ApiConstants.SUCCESS) -> ResponseTemplate[T]:
        return cls.of(data=data, message=message, code=cls.SUCCESS_CODE)

    @classmethod
    def error(
        cls,
        message: str,
        code: str = "400",
        data: Optional[Any] = None,
    ) -> ResponseTemplate[Any]:
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
