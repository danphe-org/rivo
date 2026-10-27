from typing import Optional
from sqlalchemy import String
from sqlalchemy.orm import Mapped, mapped_column

from app.common.models import BaseModel


class User(BaseModel):
    __tablename__ = "users"

    name: Mapped[str] = mapped_column(String(100), nullable=False)
    email: Mapped[str] = mapped_column(String(255), unique=True, index=True, nullable=False)
    phone: Mapped[Optional[str]] = mapped_column(String(30), nullable=True)
    role: Mapped[str] = mapped_column(String(50), default="USER", nullable=False)
