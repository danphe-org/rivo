"""Common database base models and mixins.

Mirrors the bazar-backend standards:
- com.danphe.bazar.common.library.base.modal.AuditableModal
- com.danphe.bazar.common.audits.AuditEntity
"""
import uuid
from datetime import datetime, timezone
from typing import Any, Dict, Optional

from sqlalchemy import BigInteger, Boolean, DateTime, String, Text, Uuid
from sqlalchemy.ext.hybrid import hybrid_property
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column


def utc_now() -> datetime:
    """Return current timezone-aware UTC datetime."""
    return datetime.now(timezone.utc)


class Base(DeclarativeBase):
    """Root declarative base class for all SQLAlchemy ORM entities."""
    pass


class AuditMixin:
    """Reusable audit mixin providing audit trail and soft-delete capabilities.

    Mirrors bazar-backend AuditableModal & AuditEntity:
    - created_by, created_on
    - last_modified_by, last_modified_on (with modified_by/modified_on aliases)
    - deleted_by, deleted_on
    - active, deleted
    - remark, version
    """

    created_by: Mapped[Optional[str]] = mapped_column(
        String(36),
        nullable=True,
        default=None,
        comment="Identifier of the user who created this record",
    )
    created_on: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        default=utc_now,
        nullable=False,
        comment="Timestamp when record was created",
    )

    last_modified_by: Mapped[Optional[str]] = mapped_column(
        String(36),
        nullable=True,
        default=None,
        comment="Identifier of the user who last modified this record",
    )
    last_modified_on: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        default=utc_now,
        onupdate=utc_now,
        nullable=False,
        comment="Timestamp when record was last modified",
    )

    deleted_by: Mapped[Optional[str]] = mapped_column(
        String(36),
        nullable=True,
        default=None,
        comment="Identifier of the user who soft-deleted this record",
    )
    deleted_on: Mapped[Optional[datetime]] = mapped_column(
        DateTime(timezone=True),
        nullable=True,
        default=None,
        comment="Timestamp when record was soft-deleted",
    )

    active: Mapped[bool] = mapped_column(
        Boolean,
        default=True,
        nullable=False,
        comment="Flag indicating if the record is active",
    )
    deleted: Mapped[bool] = mapped_column(
        Boolean,
        default=False,
        nullable=False,
        comment="Flag indicating if the record has been soft-deleted",
    )

    remark: Mapped[Optional[str]] = mapped_column(
        Text,
        nullable=True,
        default=None,
        comment="Optional audit remarks or operation notes",
    )
    version: Mapped[int] = mapped_column(
        BigInteger,
        default=1,
        nullable=False,
        comment="Optimistic locking version counter",
    )

    # Aliases for convenience and backward compatibility
    @hybrid_property
    def modified_on(self) -> datetime:
        return self.last_modified_on

    @modified_on.setter
    def modified_on(self, value: datetime) -> None:
        self.last_modified_on = value

    @hybrid_property
    def modified_by(self) -> Optional[str]:
        return self.last_modified_by

    @modified_by.setter
    def modified_by(self, value: Optional[str]) -> None:
        self.last_modified_by = value

    def soft_delete(self, deleted_by: Optional[str] = None) -> None:
        """Flag the record as soft-deleted rather than removing it from the database."""
        self.deleted = True
        self.active = False
        self.deleted_on = utc_now()
        self.deleted_by = deleted_by

    def restore(self) -> None:
        """Restore a previously soft-deleted record."""
        self.deleted = False
        self.active = True
        self.deleted_on = None
        self.deleted_by = None


class BaseModel(Base, AuditMixin):
    """Abstract base model implementing project-wide audit standards.

    Every concrete module model extends this class so that it carries:
    - UUID primary key (id)
    - Full audit trail (created_by, created_on, last_modified_by, last_modified_on)
    - Soft-delete flags (deleted_by, deleted_on, active, deleted)
    - Optimistic locking version and remark
    - Helper methods (soft_delete(), restore(), to_dict())
    """

    __abstract__ = True

    id: Mapped[uuid.UUID] = mapped_column(
        Uuid(as_uuid=True),
        primary_key=True,
        default=uuid.uuid4,
        comment="UUID primary key",
    )

    def __init__(self, **kwargs: Any) -> None:
        """Initialize model attributes with default audit values for direct Python instantiation."""
        if "id" not in kwargs:
            kwargs["id"] = uuid.uuid4()
        if "active" not in kwargs:
            kwargs["active"] = True
        if "deleted" not in kwargs:
            kwargs["deleted"] = False
        if "version" not in kwargs:
            kwargs["version"] = 1
        if "created_on" not in kwargs:
            kwargs["created_on"] = utc_now()
        if "last_modified_on" not in kwargs:
            kwargs["last_modified_on"] = utc_now()
        super().__init__(**kwargs)

    def to_dict(self) -> Dict[str, Any]:
        """Convert all table column values to a dictionary."""
        return {
            column.name: getattr(self, column.name)
            for column in self.__table__.columns
        }

    def __repr__(self) -> str:
        attrs = [f"id={self.id!r}"]
        if hasattr(self, "deleted"):
            attrs.append(f"deleted={self.deleted!r}")
        if hasattr(self, "active"):
            attrs.append(f"active={self.active!r}")
        return f"<{self.__class__.__name__}({', '.join(attrs)})>"
