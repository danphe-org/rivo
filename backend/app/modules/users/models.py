from typing import Any, List, Optional
import uuid
from sqlalchemy import ForeignKey, String, Uuid
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.common.models import BaseModel


class UserRole(BaseModel):
    __tablename__ = "user_roles"

    user_id: Mapped[uuid.UUID] = mapped_column(
        Uuid(as_uuid=True),
        ForeignKey("users.id", ondelete="CASCADE"),
        nullable=False,
        index=True,
    )
    role: Mapped[str] = mapped_column(String(50), default="USER", nullable=False, index=True)

    user: Mapped["User"] = relationship("User", back_populates="roles")


class User(BaseModel):
    __tablename__ = "users"

    name: Mapped[str] = mapped_column(String(100), nullable=False)
    email: Mapped[str] = mapped_column(String(255), unique=True, index=True, nullable=False)
    password: Mapped[Optional[str]] = mapped_column(String(255), nullable=True)
    phone: Mapped[Optional[str]] = mapped_column(String(30), nullable=True)

    roles: Mapped[List[UserRole]] = relationship(
        "UserRole",
        back_populates="user",
        cascade="all, delete-orphan",
        lazy="selectin",
    )

    def __init__(self, **kwargs: Any) -> None:
        role_val = kwargs.pop("role", None)
        super().__init__(**kwargs)
        if role_val:
            self.role = role_val

    @property
    def role(self) -> str:
        if self.roles:
            for r in self.roles:
                if not r.deleted and r.active:
                    return r.role
            return self.roles[0].role
        return "USER"

    @role.setter
    def role(self, new_role: str) -> None:
        if not new_role:
            return
        role_upper = new_role.strip().upper()
        if self.roles:
            for r in self.roles:
                if not r.deleted and r.active:
                    r.role = role_upper
                    return
            self.roles[0].role = role_upper
        else:
            self.roles.append(UserRole(user_id=self.id, role=role_upper))
