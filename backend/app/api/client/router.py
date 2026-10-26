"""Client Portal router aggregator (Store / Merchant / Customer)."""
from fastapi import APIRouter

from app.api.client import ping

router = APIRouter()

# Mount ping endpoint under /api/client/ping
router.include_router(ping.router)
