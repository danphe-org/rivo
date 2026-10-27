import uuid
from typing import Optional
from fastapi import APIRouter, Body, status

from app.common.pagination import Page
from app.common.schemas import ResponseTemplate, RestResponse
from app.modules.users.schemas import (
    UserCreateDTO,
    UserFilter,
    UserResponseDTO,
    UserUpdateDTO,
)
from app.modules.users.service import user_service

router = APIRouter(prefix="/users", tags=["System - Users"])


@router.post(
    "",
    response_model=ResponseTemplate[UserResponseDTO],
    status_code=status.HTTP_201_CREATED,
)
def create_user(
    payload: UserCreateDTO,
) -> ResponseTemplate[UserResponseDTO]:
    result = user_service.create_user(dto=payload)
    return RestResponse.ok(result, message="User created successfully")


@router.get(
    "/{user_id}",
    response_model=ResponseTemplate[UserResponseDTO],
    status_code=status.HTTP_200_OK,
)
def get_user_by_id(
    user_id: uuid.UUID,
) -> ResponseTemplate[UserResponseDTO]:
    result = user_service.get_user_by_id(user_id=user_id)
    return RestResponse.ok(result)


@router.put(
    "/{user_id}",
    response_model=ResponseTemplate[UserResponseDTO],
    status_code=status.HTTP_200_OK,
)
def update_user(
    user_id: uuid.UUID,
    payload: UserUpdateDTO,
) -> ResponseTemplate[UserResponseDTO]:
    result = user_service.update_user(user_id=user_id, dto=payload)
    return RestResponse.ok(result, message="User updated successfully")


@router.delete(
    "/{user_id}",
    response_model=ResponseTemplate[None],
    status_code=status.HTTP_200_OK,
)
def delete_user(
    user_id: uuid.UUID,
) -> ResponseTemplate[None]:
    user_service.delete_user(user_id=user_id)
    return RestResponse.ok(None, message="User deleted successfully")


@router.post(
    "/search",
    response_model=ResponseTemplate[Page[UserResponseDTO]],
    status_code=status.HTTP_200_OK,
)
def search_users(
    criteria: UserFilter,
) -> ResponseTemplate[Page[UserResponseDTO]]:
    result = user_service.search_users(criteria=criteria)
    return RestResponse.ok(result)


@router.post(
    "/list",
    response_model=ResponseTemplate[Page[UserResponseDTO]],
    status_code=status.HTTP_200_OK,
)
@router.get(
    "",
    response_model=ResponseTemplate[Page[UserResponseDTO]],
    status_code=status.HTTP_200_OK,
)
def list_users(
    payload: Optional[UserFilter] = Body(default_factory=UserFilter),
) -> ResponseTemplate[Page[UserResponseDTO]]:
    criteria = payload if payload is not None else UserFilter()
    result = user_service.search_users(criteria=criteria)
    return RestResponse.ok(result)
