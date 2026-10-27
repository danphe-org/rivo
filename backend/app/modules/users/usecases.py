from dataclasses import dataclass
import re
from typing import Optional, Set, Tuple
import uuid

from app.common.pagination import Page
from app.common.usecase import UseCase
from app.core.exceptions import AppException, NotFoundException
from app.core.security import hash_password
from app.modules.users.models import User, UserRole
from app.modules.users.repository import UserRepository
from app.modules.users.schemas import (
    UserCreateDTO,
    UserFilter,
    UserRegisterDTO,
    UserResponseDTO,
    UserUpdateDTO,
)

VALID_USER_ROLES: Set[str] = {"USER", "ADMIN", "STAFF", "MERCHANT", "CUSTOMER"}


def _validate_phone_format(phone: str) -> None:
    digits_only = re.sub(r"[\s\-\(\)\+]", "", phone)
    if not digits_only.isdigit() or len(digits_only) < 7 or len(digits_only) > 20:
        raise AppException("Invalid phone number format")


def _validate_strict_password(password: str) -> None:
    if not password or len(password) < 8:
        raise AppException("Password must be at least 8 characters long")
    if len(password) > 128:
        raise AppException("Password must not exceed 128 characters")
    if any(c.isspace() for c in password):
        raise AppException("Password must not contain whitespace")
    if not any(c.isupper() for c in password):
        raise AppException("Password must contain at least one uppercase letter")
    if not any(c.islower() for c in password):
        raise AppException("Password must contain at least one lowercase letter")
    if not any(c.isdigit() for c in password):
        raise AppException("Password must contain at least one number")
    if not any(c in "!@#$%^&*()_+-=[]{};':\",.<>/?`~|\\" for c in password):
        raise AppException("Password must contain at least one special character")


def _check_user_conflicts(
    repo: UserRepository,
    email: Optional[str] = None,
    phone: Optional[str] = None,
    exclude_id: Optional[uuid.UUID] = None,
) -> Tuple[Optional[User], Optional[User]]:
    email_conflict = None
    phone_conflict = None
    if hasattr(repo, "find_conflicts"):
        res = repo.find_conflicts(email=email, phone=phone, exclude_id=exclude_id)
        if isinstance(res, tuple) and len(res) == 2:
            email_conflict, phone_conflict = res
    if email_conflict is None and phone_conflict is None:
        if email and hasattr(repo, "get_by_email"):
            email_conflict = repo.get_by_email(email, include_deleted=True)
        if phone and hasattr(repo, "get_by_phone"):
            phone_conflict = repo.get_by_phone(phone, include_deleted=True)
    return email_conflict, phone_conflict


@dataclass
class RegisterUserCommand:
    dto: UserRegisterDTO
    created_by: Optional[str] = None


class RegisterUserUseCase(UseCase[RegisterUserCommand, UserResponseDTO]):

    def __init__(self, repo: UserRepository) -> None:
        self.repo = repo

    def validate(self, request: RegisterUserCommand) -> None:
        dto = request.dto

        if not dto.name or not dto.name.strip():
            raise AppException("User full name is required and cannot be blank")
        cleaned_name = dto.name.strip()
        if len(cleaned_name) < 2:
            raise AppException("User full name must be at least 2 characters long")
        if len(cleaned_name) > 100:
            raise AppException("User full name must not exceed 100 characters")

        if not dto.email or not str(dto.email).strip():
            raise AppException("Email address is required")
        normalized_email = str(dto.email).lower().strip()
        if "@" not in normalized_email or "." not in normalized_email.split("@")[-1]:
            raise AppException("Invalid email address format")
        if len(normalized_email) > 255:
            raise AppException("Email address must not exceed 255 characters")

        if dto.password is not None:
            _validate_strict_password(dto.password)

        cleaned_phone = None
        if dto.phone:
            cleaned_phone = dto.phone.strip()
            _validate_phone_format(cleaned_phone)

        email_conflict, phone_conflict = _check_user_conflicts(
            self.repo,
            email=normalized_email,
            phone=cleaned_phone,
        )
        if email_conflict:
            raise AppException("A user with this email address already exists")
        if phone_conflict:
            raise AppException("A user with this phone number already exists")

    def process(self, request: RegisterUserCommand) -> UserResponseDTO:
        dto = request.dto
        normalized_email = str(dto.email).lower().strip()
        role = "USER"
        hashed_pw = hash_password(dto.password) if dto.password else None

        user = User(
            name=dto.name.strip(),
            email=normalized_email,
            password=hashed_pw,
            phone=dto.phone.strip() if dto.phone else None,
        )
        user_role = UserRole(
            user_id=user.id,
            role=role,
        )
        user_role = self.prepare_domain(
            user_role,
            is_create=True,
            actor=request.created_by or "SELF_REGISTRATION",
            remark="User role assigned on registration",
        )
        user.roles.append(user_role)

        user = self.prepare_domain(
            user,
            is_create=True,
            actor=request.created_by or "SELF_REGISTRATION",
            remark="User self-registration",
        )

        saved_user = self.repo.save(user)
        return UserResponseDTO.model_validate(saved_user)


@dataclass
class CreateUserCommand:
    dto: UserCreateDTO
    created_by: Optional[str] = None


class CreateUserUseCase(UseCase[CreateUserCommand, UserResponseDTO]):

    def __init__(self, repo: UserRepository) -> None:
        self.repo = repo

    def validate(self, request: CreateUserCommand) -> None:
        dto = request.dto

        if not dto.name or not dto.name.strip():
            raise AppException("User full name is required and cannot be blank")
        cleaned_name = dto.name.strip()
        if len(cleaned_name) < 2:
            raise AppException("User full name must be at least 2 characters long")
        if len(cleaned_name) > 100:
            raise AppException("User full name must not exceed 100 characters")

        if not dto.email or not str(dto.email).strip():
            raise AppException("Email address is required")
        normalized_email = str(dto.email).lower().strip()
        if "@" not in normalized_email or "." not in normalized_email.split("@")[-1]:
            raise AppException("Invalid email address format")
        if len(normalized_email) > 255:
            raise AppException("Email address must not exceed 255 characters")

        if dto.password is not None:
            _validate_strict_password(dto.password)

        cleaned_phone = None
        if dto.phone:
            cleaned_phone = dto.phone.strip()
            _validate_phone_format(cleaned_phone)

        email_conflict, phone_conflict = _check_user_conflicts(
            self.repo,
            email=normalized_email,
            phone=cleaned_phone,
        )
        if email_conflict:
            raise AppException("A user with this email address already exists")
        if phone_conflict:
            raise AppException("A user with this phone number already exists")

        role_upper = dto.role.strip().upper() if dto.role else "USER"
        if role_upper not in VALID_USER_ROLES:
            raise AppException(f"Invalid user role: {dto.role}. Allowed roles: {', '.join(sorted(VALID_USER_ROLES))}")

    def process(self, request: CreateUserCommand) -> UserResponseDTO:
        dto = request.dto
        normalized_email = str(dto.email).lower().strip()
        hashed_pw = hash_password(dto.password) if dto.password else None
        role = dto.role.strip().upper() if dto.role else "USER"

        user = User(
            name=dto.name.strip(),
            email=normalized_email,
            password=hashed_pw,
            phone=dto.phone.strip() if dto.phone else None,
        )
        user_role = UserRole(
            user_id=user.id,
            role=role,
        )
        user_role = self.prepare_domain(
            user_role,
            is_create=True,
            actor=request.created_by,
            remark="User role assigned via system portal",
        )
        user.roles.append(user_role)

        user = self.prepare_domain(
            user,
            is_create=True,
            actor=request.created_by,
            remark="User creation via system portal",
        )
        saved_user = self.repo.save(user)
        return UserResponseDTO.model_validate(saved_user)


class GetUserUseCase(UseCase[uuid.UUID, UserResponseDTO]):

    def __init__(self, repo: UserRepository) -> None:
        self.repo = repo
        self._cached_user: Optional[User] = None

    def validate(self, request: uuid.UUID) -> None:
        user = self.repo.get_by_id(request)
        if not user:
            raise NotFoundException(f"User not found with ID: {request}")
        self._cached_user = user

    def process(self, request: uuid.UUID) -> UserResponseDTO:
        user = self._cached_user or self.repo.get_by_id(request)
        return UserResponseDTO.model_validate(user)


@dataclass
class UpdateUserCommand:
    user_id: uuid.UUID
    dto: UserUpdateDTO
    modified_by: Optional[str] = None


class UpdateUserUseCase(UseCase[UpdateUserCommand, UserResponseDTO]):

    def __init__(self, repo: UserRepository) -> None:
        self.repo = repo
        self._cached_user: Optional[User] = None

    def validate(self, request: UpdateUserCommand) -> None:
        user = self.repo.get_by_id(request.user_id)
        if not user:
            raise NotFoundException(f"User not found with ID: {request.user_id}")

        if request.dto.name is not None:
            cleaned_name = request.dto.name.strip()
            if not cleaned_name:
                raise AppException("User full name cannot be blank")
            if len(cleaned_name) < 2:
                raise AppException("User full name must be at least 2 characters long")
            if len(cleaned_name) > 100:
                raise AppException("User full name must not exceed 100 characters")

        check_email = None
        if request.dto.email is not None:
            normalized_email = str(request.dto.email).lower().strip()
            if "@" not in normalized_email or "." not in normalized_email.split("@")[-1]:
                raise AppException("Invalid email address format")
            if normalized_email != user.email.lower():
                check_email = normalized_email

        if request.dto.password is not None:
            _validate_strict_password(request.dto.password)

        check_phone = None
        if request.dto.phone is not None:
            cleaned_phone = request.dto.phone.strip()
            if cleaned_phone:
                _validate_phone_format(cleaned_phone)
                if not user.phone or user.phone.strip() != cleaned_phone:
                    check_phone = cleaned_phone

        if check_email or check_phone:
            email_conflict, phone_conflict = _check_user_conflicts(
                self.repo,
                email=check_email,
                phone=check_phone,
                exclude_id=user.id,
            )
            if email_conflict:
                raise AppException("A user with this email address already exists")
            if phone_conflict:
                raise AppException("A user with this phone number already exists")

        if request.dto.role is not None:
            role_upper = request.dto.role.strip().upper()
            if role_upper not in VALID_USER_ROLES:
                raise AppException(f"Invalid user role: {request.dto.role}. Allowed roles: {', '.join(sorted(VALID_USER_ROLES))}")

        self._cached_user = user

    def process(self, request: UpdateUserCommand) -> UserResponseDTO:
        user = self._cached_user or self.repo.get_by_id(request.user_id)
        dto = request.dto

        if dto.email is not None:
            user.email = str(dto.email).lower().strip()

        if dto.name is not None:
            user.name = dto.name.strip()

        if dto.password is not None:
            user.password = hash_password(dto.password)

        if dto.phone is not None:
            user.phone = dto.phone.strip() if dto.phone else None

        if dto.role is not None:
            new_role = dto.role.strip().upper()
            found = False
            for r in user.roles:
                if not r.deleted and r.active:
                    r.role = new_role
                    self.prepare_domain(r, is_create=False, actor=request.modified_by)
                    found = True
                    break
            if not found:
                new_user_role = UserRole(user_id=user.id, role=new_role)
                new_user_role = self.prepare_domain(
                    new_user_role,
                    is_create=True,
                    actor=request.modified_by,
                    remark="User role assigned on update",
                )
                user.roles.append(new_user_role)

        if dto.active is not None:
            user.active = dto.active

        user = self.prepare_domain(
            user,
            is_create=False,
            actor=request.modified_by,
        )

        saved_user = self.repo.save(user)
        return UserResponseDTO.model_validate(saved_user)


@dataclass
class DeleteUserCommand:
    user_id: uuid.UUID
    deleted_by: Optional[str] = None


class DeleteUserUseCase(UseCase[DeleteUserCommand, None]):

    def __init__(self, repo: UserRepository) -> None:
        self.repo = repo
        self._cached_user: Optional[User] = None

    def validate(self, request: DeleteUserCommand) -> None:
        user = self.repo.get_by_id(request.user_id)
        if not user:
            raise NotFoundException(f"User not found with ID: {request.user_id}")
        self._cached_user = user

    def process(self, request: DeleteUserCommand) -> None:
        user = self._cached_user or self.repo.get_by_id(request.user_id)
        if user.roles:
            for r in user.roles:
                r.soft_delete(deleted_by=request.deleted_by)
        self.repo.soft_delete(user, deleted_by=request.deleted_by)


class SearchUsersUseCase(UseCase[UserFilter, Page[UserResponseDTO]]):

    def __init__(self, repo: UserRepository) -> None:
        self.repo = repo

    def validate(self, request: UserFilter) -> None:
        if request.pageNumber < 1:
            raise AppException("pageNumber must be greater than or equal to 1")
        if request.pageSize < 1:
            raise AppException("pageSize must be greater than or equal to 1")

    def process(self, request: UserFilter) -> Page[UserResponseDTO]:
        records, total_count = self.repo.search(request)
        items = [UserResponseDTO.model_validate(u) for u in records]
        return Page[UserResponseDTO](
            totalRecords=total_count,
            limit=request.pageSize,
            currentPage=request.pageNumber,
            data=items,
        )
