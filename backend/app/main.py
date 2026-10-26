"""Production-ready FastAPI application factory matching bazar-backend standards."""
import time
from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware

from app.api.router import api_router
from app.common.schemas import ResponseTemplate, RestResponse
from app.core.config import settings
from app.core.database import check_db_connection
from app.core.exceptions import register_exception_handlers

app = FastAPI(
    title=settings.PROJECT_NAME,
    description="Production-grade RESTful API backend matching bazar-backend standards",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
    openapi_url="/openapi.json",
    debug=settings.DEBUG,
)

# 1. Global CORS Middleware configuration matching bazar-backend
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# 2. Performance & Request Timing Middleware
@app.middleware("http")
async def add_process_time_header(request: Request, call_next):
    start_time = time.perf_counter()
    response = await call_next(request)
    process_time = time.perf_counter() - start_time
    response.headers["X-Process-Time"] = f"{process_time:.4f}s"
    return response


# 3. Register standard global exception handlers
register_exception_handlers(app)

# 4. Mount Central API Router (/api/system, /api/client, /api/v1)
app.include_router(api_router)


# 5. Root Health Endpoint
@app.get(
    "/health",
    response_model=ResponseTemplate[dict],
    summary="Health check",
    tags=["Health"],
)
def health_check() -> ResponseTemplate[dict]:
    """Basic health check endpoint for container orchestrators (Docker, Kubernetes)."""
    db_ok = check_db_connection()
    return RestResponse.ok(
        {
            "status": "healthy" if db_ok else "degraded",
            "environment": settings.ENVIRONMENT,
            "database": "connected" if db_ok else "disconnected",
        }
    )


@app.get(
    "/",
    response_model=ResponseTemplate[dict],
    include_in_schema=False,
)
def root() -> ResponseTemplate[dict]:
    """Root redirect / ping."""
    return RestResponse.ok({"message": "Welcome to Rivo API", "docs": "/docs"})
