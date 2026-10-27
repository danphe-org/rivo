from contextvars import ContextVar
from typing import Generator, Optional
from fastapi import Request, Response
from sqlalchemy.orm import Session
from starlette.middleware.base import BaseHTTPMiddleware

from app.core.database import SessionLocal

_session_context: ContextVar[Optional[Session]] = ContextVar("_session_context", default=None)


def get_current_session() -> Session:
    session = _session_context.get()
    if session is None:
        session = SessionLocal()
        _session_context.set(session)
    return session


def set_current_session(session: Optional[Session]):
    return _session_context.set(session)


def reset_current_session(token) -> None:
    _session_context.reset(token)


class DatabaseSessionMiddleware(BaseHTTPMiddleware):

    async def dispatch(self, request: Request, call_next) -> Response:
        session: Session = SessionLocal()
        token = _session_context.set(session)
        try:
            response = await call_next(request)
            return response
        except Exception:
            session.rollback()
            raise
        finally:
            session.close()
            _session_context.reset(token)


def get_db() -> Generator[Session, None, None]:
    session = get_current_session()
    yield session


__all__ = [
    "get_current_session",
    "set_current_session",
    "reset_current_session",
    "DatabaseSessionMiddleware",
    "get_db",
]
