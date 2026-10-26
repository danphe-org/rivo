"""Database session lifecycle and request dependency."""
from typing import Generator
from sqlalchemy.orm import Session

from app.core.database import SessionLocal


def get_db() -> Generator[Session, None, None]:
    """FastAPI dependency yielding a transactional database session per request.

    Ensures that every request receives its own isolated database session
    and reliably closes it once the request finishes.
    """
    db: Session = SessionLocal()
    try:
        yield db
    finally:
        db.close()


__all__ = ["get_db"]
