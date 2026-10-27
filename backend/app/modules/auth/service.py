from typing import Optional
import uuid

from app.modules.auth.schemas import (
    LoginDTO,
    LoginResponseDTO,
    TokenResponseDTO,
)
from app.modules.auth.usecases import (
    GetCurrentUserUseCase,
    LoginCommand,
    LoginUseCase,
    RefreshTokenCommand,
    RefreshTokenUseCase,
)
from app.modules.users.repository import UserRepository, user_repository
from app.modules.users.schemas import UserResponseDTO


class AuthService:

    def __init__(self, user_repo: UserRepository = user_repository) -> None:
        self.user_repo = user_repo

    def login(
        self,
        dto: LoginDTO,
        portal: str = "CLIENT",
        store_id: Optional[str] = None,
    ) -> LoginResponseDTO:
        use_case = LoginUseCase(self.user_repo)
        return use_case.execute(LoginCommand(dto=dto, portal=portal, store_id=store_id))

    def refresh_token(
        self,
        refresh_token: Optional[str],
        store_id: Optional[str] = None,
    ) -> TokenResponseDTO:
        use_case = RefreshTokenUseCase(self.user_repo)
        return use_case.execute(RefreshTokenCommand(refresh_token=refresh_token, store_id=store_id))

    def get_current_user_profile(self, user_id: uuid.UUID) -> UserResponseDTO:
        use_case = GetCurrentUserUseCase(self.user_repo)
        return use_case.execute(user_id)


auth_service = AuthService()
