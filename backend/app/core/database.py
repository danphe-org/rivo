from sqlalchemy import create_engine, text
from sqlalchemy.engine import Engine
from sqlalchemy.orm import sessionmaker

from app.core.config import settings
from app.core.logging import logger

engine: Engine = create_engine(
    settings.SQLALCHEMY_DATABASE_URL,
    pool_size=settings.get_int("DB_POOL_SIZE", 10),
    max_overflow=settings.get_int("DB_MAX_OVERFLOW", 20),
    pool_timeout=settings.get_int("DB_POOL_TIMEOUT", 30),
    pool_recycle=settings.get_int("DB_POOL_RECYCLE", 1800),
    pool_pre_ping=settings.get_bool("DB_POOL_PRE_PING", False),
    echo=settings.get_bool("DB_ECHO", False),
)

SessionLocal = sessionmaker(
    autocommit=False,
    autoflush=False,
    bind=engine,
    expire_on_commit=False,
)


def check_db_connection() -> bool:
    try:
        with engine.connect() as connection:
            connection.execute(text("SELECT 1"))
        return True
    except Exception as exc:
        logger.error(f"Database connection verification failed: {exc}")
        return False


__all__ = ["engine", "SessionLocal", "check_db_connection"]
