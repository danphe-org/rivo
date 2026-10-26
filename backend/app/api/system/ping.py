"""System Portal Ping & Health check endpoint."""
from fastapi import APIRouter, status

from app.common.schemas import ResponseTemplate, RestResponse

router = APIRouter(prefix="/ping", tags=["System - Health"])


@router.get(
    "",
    response_model=ResponseTemplate[str],
    status_code=status.HTTP_200_OK,
    summary="System portal health ping",
    description="Back-office system portal health verification endpoint. Returns 'pong'.",
)
def system_ping() -> ResponseTemplate[str]:
    """Return system portal ping response."""
    return RestResponse.ok("pong")
