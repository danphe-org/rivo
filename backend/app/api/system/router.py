"""System Portal router aggregator (Back-office / Admin)."""
from fastapi import APIRouter

from app.api.system import ping

router = APIRouter()

# Mount ping endpoint under /api/system/ping
router.include_router(ping.router)
