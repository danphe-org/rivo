from fastapi import APIRouter

from app.api.client import ping, users

router = APIRouter()
router.include_router(ping.router)
router.include_router(users.router)
