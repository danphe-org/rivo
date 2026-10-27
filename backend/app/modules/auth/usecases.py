from dataclasses import dataclass
from typing import Optional
import uuid

from app.common.usecase import UseCase
from app.core.exceptions import AppException, ForbiddenException, UnauthorizedException
from app.core.security import JwtTokenProvider, jwt_token_provider, verify_password
from app.modules.auth.schemas import (
    LoginDTO,
    LoginResponseDTO,
    TokenResponseDTO,
)
from app.modules.users.models import User
from app.modules.users.repository import UserRepository, user_repository
from app.modules.users.schemas import UserResponseDTO


@dataclass
class LoginCommand:
    dto: LoginDTO
    portal: str = "CLIENT"
    store_id: Optional[str] = None


class LoginUseCase(UseCase[LoginCommand, LoginResponseDTO]):

    def __init__(
        self,
        user_repo: UserRepository = user_repository,
        jwt_provider: JwtTokenProvider = jwt_token_provider,
    ) -> None:
        self.user_repo = user_repo
        self.jwt_provider = jwt_provider
        self._cached_user: Optional[User] = None

    def validate(self, request: LoginCommand) -> None:
        dto = request.dto
        if not dto.email or not str(dto.email).strip():
            raise AppException("Email is required")
        if not dto.password:
            raise AppException("Password is required")

        normalized_email = str(dto.email).lower().strip()
        user = self.user_repo.get_by_email(normalized_email, include_deleted=True)

        if not user or user.deleted:
            raise AppException("Invalid email or password")

        if not user.active:
            raise AppException("User account is inactive")

        if not user.password or not verify_password(dto.password, user.password):
            raise AppException("Invalid email or password")

        if request.portal.upper() == "SYSTEM":
            user_roles = {r.role.upper() for r in user.roles if r.active and not r.deleted}
            if user.role:
                user_roles.add(user.role.upper())
            if not user_roles.intersection({"ADMIN", "STAFF"}):
                raise ForbiddenException("Access denied: Administrative privileges required for system portal")

        self._cached_user = user

    def process(self, request: LoginCommand) -> LoginResponseDTO:
        user = self._cached_user or self.user_repo.get_by_email(str(request.dto.email).lower().strip())
        access_token = self.jwt_provider.generate_access_token(str(user.id), request.store_id)
        refresh_token = self.jwt_provider.generate_refresh_token(str(user.id), request.store_id)

        user_response = UserResponseDTO.model_validate(user)
        return LoginResponseDTO(
            **user_response.model_dump(),
            access_token=access_token,
            refresh_token=refresh_token,
        )


@dataclass
class RefreshTokenCommand:
    refresh_token: Optional[str]
    store_id: Optional[str] = None


class RefreshTokenUseCase(UseCase[RefreshTokenCommand, TokenResponseDTO]):

    def __init__(
        self,
        user_repo: UserRepository = user_repository,
        jwt_provider: JwtTokenProvider = jwt_token_provider,
    ) -> None:
        self.user_repo = user_repo
        self.jwt_provider = jwt_provider
        self._cached_user: Optional[User] = None
        self._extracted_store_id: Optional[str] = None

    def validate(self, request: RefreshTokenCommand) -> None:
        if not request.refresh_token:
            raise UnauthorizedException("Refresh token missing")

        if not self.jwt_provider.is_valid(request.refresh_token):
            raise UnauthorizedException("Invalid refresh token")

        try:
            user_id_str = self.jwt_provider.extract_user_id(request.refresh_token)
            user_uuid = uuid.UUID(user_id_str)
        except Exception:
            raise UnauthorizedException("Invalid refresh token")

        user = self.user_repo.get_by_id(user_uuid)
        if not user or user.deleted:
            raise UnauthorizedException("User not found or inactive")

        if not user.active:
            raise UnauthorizedException("User account is inactive")

        self._cached_user = user
        self._extracted_store_id = self.jwt_provider.extract_store_id(request.refresh_token)

    def process(self, request: RefreshTokenCommand) -> TokenResponseDTO:
        user = self._cached_user
        store_id = request.store_id or self._extracted_store_id
        new_access = self.jwt_provider.generate_access_token(str(user.id), store_id)
        new_refresh = self.jwt_provider.generate_refresh_token(str(user.id), store_id)
        return TokenResponseDTO(
            access_token=new_access,
            refresh_token=new_refresh,
        )


class GetCurrentUserUseCase(UseCase[uuid.UUID, UserResponseDTO]):

    def __init__(self, user_repo: UserRepository = user_repository) -> None:
        self.user_repo = user_repo
        self._cached_user: Optional[User] = None

    def validate(self, request: uuid.UUID) -> None:
        user = self.user_repo.get_by_id(request)
        if not user or user.deleted:
            raise UnauthorizedException("User not found")
        if not user.active:
            raise UnauthorizedException("User account is inactive")
        self._cached_user = user

    def process(self, request: uuid.UUID) -> UserResponseDTO:
        return UserResponseDTO.model_validate(self._cached_user)
