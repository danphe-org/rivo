"""Database metadata registry.

Import all module models here so that Alembic and SQLAlchemy metadata
can detect and register all entities across modules.
"""
from app.common.models import Base, BaseModel

__all__ = ["Base", "BaseModel"]
