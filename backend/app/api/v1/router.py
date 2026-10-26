"""API v1 router aggregator."""
from fastapi import APIRouter

from app.api.v1 import ping

router = APIRouter()

# Mount ping endpoint under /api/v1
router.include_router(ping.router)
