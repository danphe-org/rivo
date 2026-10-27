import time
from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware

from app.api.router import api_router
from app.common.schemas import ResponseTemplate, RestResponse
from app.core.config import settings
from app.core.database import check_db_connection
from app.core.exceptions import register_exception_handlers
from app.db.session import DatabaseSessionMiddleware

app = FastAPI(
    title=settings.PROJECT_NAME,
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
    openapi_url="/openapi.json",
    debug=settings.DEBUG,
)

app.add_middleware(DatabaseSessionMiddleware)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.middleware("http")
async def add_process_time_header(request: Request, call_next):
    start_time = time.perf_counter()
    response = await call_next(request)
    process_time = time.perf_counter() - start_time
    response.headers["X-Process-Time"] = f"{process_time:.4f}s"
    return response


register_exception_handlers(app)

app.include_router(api_router)


@app.get(
    "/health",
    response_model=ResponseTemplate[dict],
    tags=["Health"],
)
def health_check() -> ResponseTemplate[dict]:
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
    return RestResponse.ok({"message": "Welcome to Rivo API", "docs": "/docs"})
