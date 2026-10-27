from typing import Optional
from fastapi import APIRouter, Depends, Request, Response, status

from app.api.deps import get_current_user
from app.common.schemas import ResponseTemplate, RestResponse
from app.core.config import settings
from app.core.security import jwt_token_provider
from app.modules.auth.schemas import (
    LoginDTO,
    LoginResponseDTO,
    RefreshTokenDTO,
    TokenResponseDTO,
)
from app.modules.auth.service import auth_service
from app.modules.users.models import User
from app.modules.users.schemas import UserRegisterDTO, UserResponseDTO
from app.modules.users.service import user_service

router = APIRouter(prefix="/auth", tags=["Client Auth"])


@router.post("/login", response_model=ResponseTemplate[LoginResponseDTO])
def client_login(
    dto: LoginDTO,
    response: Response,
) -> ResponseTemplate[LoginResponseDTO]:
    result = auth_service.login(dto, portal="CLIENT")
    if result.access_token and result.refresh_token:
        jwt_token_provider.set_auth_cookies(response, result.access_token, result.refresh_token)
    return RestResponse.ok(result, message="Login successful")


@router.post(
    "/register",
    response_model=ResponseTemplate[LoginResponseDTO],
    status_code=status.HTTP_201_CREATED,
)
def client_register(
    dto: UserRegisterDTO,
    response: Response,
) -> ResponseTemplate[LoginResponseDTO]:
    user_dto = user_service.register_user(dto)
    access_token = jwt_token_provider.generate_access_token(str(user_dto.id))
    refresh_token = jwt_token_provider.generate_refresh_token(str(user_dto.id))
    jwt_token_provider.set_auth_cookies(response, access_token, refresh_token)

    login_response = LoginResponseDTO(
        **user_dto.model_dump(),
        access_token=access_token,
        refresh_token=refresh_token,
    )
    return RestResponse.ok(login_response, message="User registered successfully")


@router.post("/refresh", response_model=ResponseTemplate[TokenResponseDTO])
def client_refresh_token(
    request: Request,
    response: Response,
    body: Optional[RefreshTokenDTO] = None,
) -> ResponseTemplate[TokenResponseDTO]:
    refresh_token = request.cookies.get(settings.REFRESH_TOKEN_COOKIE)
    if not refresh_token and body and body.refresh_token:
        refresh_token = body.refresh_token

    result = auth_service.refresh_token(refresh_token)
    if result.access_token and result.refresh_token:
        jwt_token_provider.set_auth_cookies(response, result.access_token, result.refresh_token)
    return RestResponse.ok(result, message="Token refreshed successfully")


@router.post("/logout", response_model=ResponseTemplate[dict])
def client_logout(
    response: Response,
) -> ResponseTemplate[dict]:
    jwt_token_provider.clear_auth_cookies(response)
    return RestResponse.ok({"message": "Logged out successfully"})


@router.get("/me", response_model=ResponseTemplate[UserResponseDTO])
def client_get_me(
    current_user: User = Depends(get_current_user),
) -> ResponseTemplate[UserResponseDTO]:
    return RestResponse.ok(UserResponseDTO.model_validate(current_user))
