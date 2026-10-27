"""Client Portal Ping & Health check endpoint."""
from fastapi import APIRouter, status

from app.common.schemas import ResponseTemplate, RestResponse

router = APIRouter(prefix="/ping", tags=["Client - Health"])


@router.get(
    "",
    response_model=ResponseTemplate[str],
    status_code=status.HTTP_200_OK,
)
def client_ping() -> ResponseTemplate[str]:
    """Return client portal ping response."""
    return RestResponse.ok("pong")
