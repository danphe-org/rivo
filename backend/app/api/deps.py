from typing import Callable, Optional
import uuid
from fastapi import Depends, Request

from app.core.config import settings
from app.core.exceptions import ForbiddenException, UnauthorizedException
from app.core.security import jwt_token_provider
from app.db.session import get_db
from app.modules.users.models import User
from app.modules.users.repository import user_repository


def resolve_token(request: Request) -> Optional[str]:
    cookie_token = request.cookies.get(settings.ACCESS_TOKEN_COOKIE)
    if cookie_token:
        return cookie_token

    auth_header = request.headers.get("Authorization")
    if auth_header and auth_header.startswith("Bearer "):
        return auth_header[7:].strip()

    return None


def get_current_user(request: Request) -> User:
    token = resolve_token(request)
    if not token:
        raise UnauthorizedException("Not authenticated")

    if not jwt_token_provider.is_valid(token):
        raise UnauthorizedException("Invalid access token")

    try:
        raw_user_id = jwt_token_provider.extract_user_id(token)
        user_uuid = uuid.UUID(raw_user_id)
    except Exception:
        raise UnauthorizedException("Invalid token payload")

    user = user_repository.get_by_id(user_uuid)
    if not user:
        raise UnauthorizedException("User not found")

    if not user.active or user.deleted:
        raise UnauthorizedException("User account is inactive")

    return user


def get_optional_current_user(request: Request) -> Optional[User]:
    token = resolve_token(request)
    if not token or not jwt_token_provider.is_valid(token):
        return None

    try:
        raw_user_id = jwt_token_provider.extract_user_id(token)
        user_uuid = uuid.UUID(raw_user_id)
        user = user_repository.get_by_id(user_uuid)
        if user and user.active and not user.deleted:
            return user
    except Exception:
        return None
    return None


def require_roles(*allowed_roles: str) -> Callable[[User], User]:
    normalized_allowed = {r.strip().upper() for r in allowed_roles}

    def role_checker(user: User = Depends(get_current_user)) -> User:
        user_roles = {r.role.strip().upper() for r in user.roles if r.active and not r.deleted}
        if user.role:
            user_roles.add(user.role.strip().upper())

        if not user_roles.intersection(normalized_allowed):
            raise ForbiddenException("Access denied: Insufficient privileges")
        return user

    return role_checker


__all__ = [
    "get_db",
    "resolve_token",
    "get_current_user",
    "get_optional_current_user",
    "require_roles",
]
