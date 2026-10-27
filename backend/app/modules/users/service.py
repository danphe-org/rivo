from typing import Optional
import uuid

from app.common.pagination import Page
from app.modules.users.repository import UserRepository, user_repository
from app.modules.users.schemas import (
    UserCreateDTO,
    UserFilter,
    UserRegisterDTO,
    UserResponseDTO,
    UserUpdateDTO,
)
from app.modules.users.usecases import (
    CreateUserCommand,
    CreateUserUseCase,
    DeleteUserCommand,
    DeleteUserUseCase,
    GetUserUseCase,
    RegisterUserCommand,
    RegisterUserUseCase,
    SearchUsersUseCase,
    UpdateUserCommand,
    UpdateUserUseCase,
)


class UserService:

    def __init__(self, repo: Optional[UserRepository] = None) -> None:
        self.repo = repo or user_repository
        self.register_user_use_case = RegisterUserUseCase(self.repo)
        self.create_user_use_case = CreateUserUseCase(self.repo)
        self.get_user_use_case = GetUserUseCase(self.repo)
        self.update_user_use_case = UpdateUserUseCase(self.repo)
        self.delete_user_use_case = DeleteUserUseCase(self.repo)
        self.search_users_use_case = SearchUsersUseCase(self.repo)

    def register_user(
        self,
        dto: UserRegisterDTO,
        created_by: Optional[str] = None,
    ) -> UserResponseDTO:
        command = RegisterUserCommand(dto=dto, created_by=created_by)
        return self.register_user_use_case.execute(command)

    def create_user(
        self,
        dto: UserCreateDTO,
        created_by: Optional[str] = None,
    ) -> UserResponseDTO:
        command = CreateUserCommand(dto=dto, created_by=created_by)
        return self.create_user_use_case.execute(command)

    def get_user_by_id(self, user_id: uuid.UUID) -> UserResponseDTO:
        return self.get_user_use_case.execute(user_id)

    def update_user(
        self,
        user_id: uuid.UUID,
        dto: UserUpdateDTO,
        modified_by: Optional[str] = None,
    ) -> UserResponseDTO:
        command = UpdateUserCommand(user_id=user_id, dto=dto, modified_by=modified_by)
        return self.update_user_use_case.execute(command)

    def delete_user(
        self,
        user_id: uuid.UUID,
        deleted_by: Optional[str] = None,
    ) -> None:
        command = DeleteUserCommand(user_id=user_id, deleted_by=deleted_by)
        self.delete_user_use_case.execute(command)

    def search_users(
        self,
        criteria: UserFilter,
    ) -> Page[UserResponseDTO]:
        return self.search_users_use_case.execute(criteria)


user_service = UserService()
