"""Ping & Health check endpoint."""
from fastapi import APIRouter, status

from app.common.schemas import ResponseTemplate, RestResponse

router = APIRouter(prefix="/ping", tags=["Health"])


@router.get(
    "",
    response_model=ResponseTemplate[str],
    status_code=status.HTTP_200_OK,
    summary="Ping server",
    description="Public connectivity and health verification endpoint. Returns 'pong'.",
)
def ping() -> ResponseTemplate[str]:
    """Return ping health check response."""
    return RestResponse.ok("pong")
