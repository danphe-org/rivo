from fastapi import APIRouter

from app.api.client.router import router as client_router
from app.api.system.router import router as system_router
from app.api.v1.router import router as v1_router

api_router = APIRouter()
api_router.include_router(system_router, prefix="/api/system")
api_router.include_router(client_router, prefix="/api/client")
api_router.include_router(v1_router, prefix="/api/v1")
