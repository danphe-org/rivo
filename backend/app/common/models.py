import uuid
from datetime import datetime, timezone
from typing import Any, Dict, Optional

from sqlalchemy import BigInteger, Boolean, DateTime, String, Text, Uuid
from sqlalchemy.ext.hybrid import hybrid_property
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column


def utc_now() -> datetime:
    return datetime.now(timezone.utc)


class Base(DeclarativeBase):
    pass


class AuditMixin:
    created_by: Mapped[Optional[str]] = mapped_column(
        String(36),
        nullable=True,
        default=None,
    )
    created_on: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        default=utc_now,
        nullable=False,
    )

    last_modified_by: Mapped[Optional[str]] = mapped_column(
        String(36),
        nullable=True,
        default=None,
    )
    last_modified_on: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        default=utc_now,
        onupdate=utc_now,
        nullable=False,
    )

    deleted_by: Mapped[Optional[str]] = mapped_column(
        String(36),
        nullable=True,
        default=None,
    )
    deleted_on: Mapped[Optional[datetime]] = mapped_column(
        DateTime(timezone=True),
        nullable=True,
        default=None,
    )

    active: Mapped[bool] = mapped_column(
        Boolean,
        default=True,
        nullable=False,
    )
    deleted: Mapped[bool] = mapped_column(
        Boolean,
        default=False,
        nullable=False,
    )

    remark: Mapped[Optional[str]] = mapped_column(
        Text,
        nullable=True,
        default=None,
    )
    version: Mapped[int] = mapped_column(
        BigInteger,
        default=1,
        nullable=False,
    )

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

    @hybrid_property
    def updated_on(self) -> datetime:
        return self.last_modified_on

    @updated_on.setter
    def updated_on(self, value: datetime) -> None:
        self.last_modified_on = value

    @hybrid_property
    def updated_by(self) -> Optional[str]:
        return self.last_modified_by

    @updated_by.setter
    def updated_by(self, value: Optional[str]) -> None:
        self.last_modified_by = value

    def soft_delete(self, deleted_by: Optional[str] = None) -> None:
        self.deleted = True
        self.active = False
        self.deleted_on = utc_now()
        self.deleted_by = deleted_by

    def restore(self) -> None:
        self.deleted = False
        self.active = True
        self.deleted_on = None
        self.deleted_by = None


class BaseModel(Base, AuditMixin):
    __abstract__ = True

    id: Mapped[uuid.UUID] = mapped_column(
        Uuid(as_uuid=True),
        primary_key=True,
        default=uuid.uuid4,
    )

    def __init__(self, **kwargs: Any) -> None:
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
