from fastapi import APIRouter

from app.api.system import ping, users

router = APIRouter()
router.include_router(ping.router)
router.include_router(users.router)
