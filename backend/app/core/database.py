"""Database engine and session management.

Configures the PostgreSQL database engine, session factory, and connection validation
using credentials loaded dynamically from .env.
"""
from sqlalchemy import create_engine, text
from sqlalchemy.engine import Engine
from sqlalchemy.orm import sessionmaker

from app.core.config import settings
from app.core.logging import logger

# Primary PostgreSQL database engine instance with production connection pool
engine: Engine = create_engine(
    settings.SQLALCHEMY_DATABASE_URL,
    pool_size=settings.get_int("DB_POOL_SIZE", 10),
    max_overflow=settings.get_int("DB_MAX_OVERFLOW", 20),
    pool_timeout=settings.get_int("DB_POOL_TIMEOUT", 30),
    pool_pre_ping=True,
    echo=settings.get_bool("DB_ECHO", False),
)

# Session factory for creating transactional database sessions
SessionLocal = sessionmaker(
    autocommit=False,
    autoflush=False,
    bind=engine,
)


def check_db_connection() -> bool:
    """Executes a ping query to verify that the database engine can establish connections."""
    try:
        with engine.connect() as connection:
            connection.execute(text("SELECT 1"))
        return True
    except Exception as exc:
        logger.error(f"Database connection verification failed: {exc}")
        return False


__all__ = ["engine", "SessionLocal", "check_db_connection"]
