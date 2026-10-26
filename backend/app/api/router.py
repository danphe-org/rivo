"""Central API router aggregating all portals matching bazar-backend."""
from fastapi import APIRouter

from app.api.client.router import router as client_router
from app.api.system.router import router as system_router
from app.api.v1.router import router as v1_router

api_router = APIRouter()

# 1. System Portal APIs (/api/system) - Back-office / Admin
api_router.include_router(system_router, prefix="/api/system")

# 2. Client Portal APIs (/api/client) - Store / Merchant / Customer
api_router.include_router(client_router, prefix="/api/client")

# 3. Versioned Feature APIs (/api/v1)
api_router.include_router(v1_router, prefix="/api/v1")
