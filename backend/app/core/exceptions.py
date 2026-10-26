from enum import Enum
import traceback
from typing import Any, Optional, Union

from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from starlette.exceptions import HTTPException as StarletteHTTPException

from app.common.utils import DateUtils
from app.core.logging import logger


class ExceptionType:
    """Base representation or contract for structured exception types."""

    def __init__(self, code: str, description: str):
        self.code = str(code)
        self.description = str(description)

    def get_code(self) -> str:
        return self.code

    def get_description(self) -> str:
        return self.description

    def __repr__(self) -> str:
        return f"ExceptionType(code='{self.code}', description='{self.description}')"


class ErrorCodeEnum(Enum):
    """Convenience Enum base where each member has (code, description)."""

    def __new__(cls, code: str, description: str):
        obj = object.__new__(cls)
        obj._value_ = code
        obj.code = str(code)
        obj.description = str(description)
        return obj

    def get_code(self) -> str:
        return self.code

    def get_description(self) -> str:
        return self.description


class AppException(Exception):
    """
    Base application exception matching bazar-backend's AppException.

    Can be instantiated in multiple ways:
      1. AppException("Something went wrong")
         -> code="400", message="Something went wrong", status_code=400
      2. AppException("ABE00001", "Insert failed")
         -> code="ABE00001", message="Insert failed", status_code=400
      3. AppException(AbstractError.DUPLICATE_ERROR)
         -> code="ABE00002", message="Duplicate entry found", status_code=400
      4. AppException(code="ERR_01", message="Custom error", status_code=422)
    """

    status_code: int = 400

    def __init__(
        self,
        first_arg: Union[str, ExceptionType, ErrorCodeEnum, Any] = "",
        second_arg: Optional[str] = None,
        *,
        code: Optional[str] = None,
        message: Optional[str] = None,
        status_code: Optional[int] = None,
    ) -> None:
        if status_code is not None:
            self.status_code = status_code

        # Case 1: Enum or ExceptionType with (code, description)
        if hasattr(first_arg, "code") and (
            hasattr(first_arg, "description") or hasattr(first_arg, "get_description")
        ):
            self.code = getattr(first_arg, "code")
            desc = getattr(first_arg, "description", None)
            if desc is None and callable(getattr(first_arg, "get_description", None)):
                desc = first_arg.get_description()
            self.message = str(desc or first_arg)

        # Case 2: AppException(code, message)
        elif second_arg is not None:
            self.code = str(first_arg)
            self.message = str(second_arg)

        # Case 3: Keyword arguments or single message
        else:
            self.code = code if code is not None else str(self.status_code)
            self.message = message if message is not None else str(first_arg)

        # Allow explicit override via keyword arguments
        if code is not None:
            self.code = code
        if message is not None:
            self.message = message

        super().__init__(self.message)

    @property
    def exception_type(self) -> ExceptionType:
        return ExceptionType(code=self.code, description=self.message)

    def get_exception_type(self) -> ExceptionType:
        return self.exception_type


# Alias for convenience / backwards compatibility
AppError = AppException


class NotFoundException(AppException):

    """Thrown when a requested entity or resource is not found (HTTP 404)."""

    status_code: int = 404

    def __init__(
        self,
        first_arg: Union[str, ExceptionType, ErrorCodeEnum, Any] = "Resource not found",
        second_arg: Optional[str] = None,
        *,
        code: Optional[str] = None,
        message: Optional[str] = None,
    ) -> None:
        super().__init__(
            first_arg,
            second_arg,
            code=code or ("404" if second_arg is None and not hasattr(first_arg, "code") else None),
            message=message,
            status_code=404,
        )


class ForbiddenException(AppException):
    """Thrown when a user lacks required permissions (HTTP 403)."""

    status_code: int = 403

    class ForbiddenError(ErrorCodeEnum):
        NOT_AUTHORIZED = ("SMG-AUT-FOR", "Not Authorized")

    def __init__(
        self,
        first_arg: Union[str, ExceptionType, ErrorCodeEnum, Any] = "Access forbidden",
        second_arg: Optional[str] = None,
        *,
        code: Optional[str] = None,
        message: Optional[str] = None,
    ) -> None:
        super().__init__(
            first_arg,
            second_arg,
            code=code or ("403" if second_arg is None and not hasattr(first_arg, "code") else None),
            message=message,
            status_code=403,
        )


class UnauthorizedException(AppException):
    """Thrown when authentication credentials are missing or invalid (HTTP 401)."""

    status_code: int = 401

    def __init__(
        self,
        first_arg: Union[str, ExceptionType, ErrorCodeEnum, Any] = "Unauthorized",
        second_arg: Optional[str] = None,
        *,
        code: Optional[str] = None,
        message: Optional[str] = None,
    ) -> None:
        super().__init__(
            first_arg,
            second_arg,
            code=code or ("401" if second_arg is None and not hasattr(first_arg, "code") else None),
            message=message,
            status_code=401,
        )


# --- Domain Error Managers matching bazar-backend ---

class AbstractError(ErrorCodeEnum):
    INSERT_ERROR = ("ABE00001", "Error insert failed")
    DUPLICATE_ERROR = ("ABE00002", "Duplicate entry found")
    RECORD_NOT_FOUND = ("ABE00003", "Record not found")
    UPDATE_ERROR = ("ABE00004", "Error update failed")


class AbstractExceptionManager(AppException):
    """Exception manager matching bazar-backend's AbstractExceptionManager."""

    AbstractError = AbstractError

    def __init__(
        self,
        exception_type: Union[AbstractError, ExceptionType, str] = AbstractError.INSERT_ERROR,
        status_code: int = 400,
    ) -> None:
        super().__init__(exception_type, status_code=status_code)


def _build_error_response(
    status_code: int,
    code: str,
    message: str,
    data: Optional[Any] = None,
) -> JSONResponse:
    """Builds a standardized error response adhering to ResponseTemplate format."""
    return JSONResponse(
        status_code=status_code,
        content={
            "code": code,
            "message": message,
            "data": data,
            "time": DateUtils.now(),
        },
    )


def register_exception_handlers(app: FastAPI, include_traceback: bool = False) -> None:
    """
    Registers global exception handlers on the FastAPI application,
    mirroring bazar-backend's GlobalExceptionHandler.
    """

    @app.exception_handler(AppException)
    async def app_exception_handler(request: Request, exc: AppException) -> JSONResponse:
        return _build_error_response(
            status_code=exc.status_code,
            code=exc.code,
            message=exc.message,
            data=None,
        )

    @app.exception_handler(RequestValidationError)
    async def validation_exception_handler(
        request: Request, exc: RequestValidationError
    ) -> JSONResponse:
        # Format field errors similarly to Spring's MethodArgumentNotValidException:
        # e.g., "email: Field required, password: String should have at least 8 characters"
        field_errors = []
        for err in exc.errors():
            loc = err.get("loc", ())
            # Filter out generic container markers like 'body', 'query', 'path'
            filtered_loc = [str(item) for item in loc if item not in ("body", "query", "path")]
            field_name = ".".join(filtered_loc) if filtered_loc else "body"
            msg = err.get("msg", "Invalid value")
            field_errors.append(f"{field_name}: {msg}")

        combined_message = ", ".join(field_errors) if field_errors else "Validation failed"
        return _build_error_response(
            status_code=400,
            code="400",
            message=combined_message,
            data=None,
        )

    @app.exception_handler(StarletteHTTPException)
    async def http_exception_handler(
        request: Request, exc: StarletteHTTPException
    ) -> JSONResponse:
        return _build_error_response(
            status_code=exc.status_code,
            code=str(exc.status_code),
            message=str(exc.detail),
            data=None,
        )

    @app.exception_handler(Exception)
    async def unhandled_exception_handler(request: Request, exc: Exception) -> JSONResponse:
        logger.exception("Unhandled exception: %s", exc)
        trace_data = (
            "".join(traceback.format_exception(type(exc), exc, exc.__traceback__))
            if include_traceback
            else None
        )
        return _build_error_response(
            status_code=500,
            code="500",
            message=str(exc) or "Internal Server Error",
            data=trace_data,
        )
