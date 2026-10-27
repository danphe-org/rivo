from typing import Optional
from fastapi import APIRouter, Depends, Request, Response

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
from app.modules.users.schemas import UserResponseDTO

router = APIRouter(prefix="/auth", tags=["System Auth"])


@router.post("/login", response_model=ResponseTemplate[LoginResponseDTO])
def system_login(
    dto: LoginDTO,
    response: Response,
) -> ResponseTemplate[LoginResponseDTO]:
    result = auth_service.login(dto, portal="SYSTEM")
    if result.access_token and result.refresh_token:
        jwt_token_provider.set_auth_cookies(response, result.access_token, result.refresh_token)
    return RestResponse.ok(result, message="Login successful")


@router.post("/refresh", response_model=ResponseTemplate[TokenResponseDTO])
def system_refresh_token(
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
def system_logout(
    response: Response,
) -> ResponseTemplate[dict]:
    jwt_token_provider.clear_auth_cookies(response)
    return RestResponse.ok({"message": "Logged out successfully"})


@router.get("/me", response_model=ResponseTemplate[UserResponseDTO])
def system_get_me(
    current_user: User = Depends(get_current_user),
) -> ResponseTemplate[UserResponseDTO]:
    return RestResponse.ok(UserResponseDTO.model_validate(current_user))
