from fastapi import APIRouter, status

from app.common.schemas import ResponseTemplate, RestResponse
from app.modules.users.schemas import UserRegisterDTO, UserResponseDTO
from app.modules.users.service import user_service

router = APIRouter(prefix="/users", tags=["Client - Users"])


@router.post(
    "/register",
    response_model=ResponseTemplate[UserResponseDTO],
    status_code=status.HTTP_201_CREATED,
)
def register_user(
    payload: UserRegisterDTO,
) -> ResponseTemplate[UserResponseDTO]:
    result = user_service.register_user(dto=payload)
    return RestResponse.ok(result, message="User registered successfully")
