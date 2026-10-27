from datetime import datetime, timedelta, timezone
from typing import Any, Dict, Optional
import bcrypt
from fastapi import Response
import jwt

from app.core.config import settings


def hash_password(password: str) -> str:
    salt = bcrypt.gensalt(rounds=10)
    return bcrypt.hashpw(password.encode("utf-8"), salt).decode("utf-8")


def verify_password(plain_password: str, hashed_password: str) -> bool:
    try:
        return bcrypt.checkpw(
            plain_password.encode("utf-8"),
            hashed_password.encode("utf-8"),
        )
    except Exception:
        return False


class JwtTokenProvider:

    def __init__(self) -> None:
        self.secret = settings.JWT_SECRET
        self.algorithm = settings.JWT_ALGORITHM
        self.access_expiry_ms = settings.JWT_ACCESS_TOKEN_EXPIRY_MS
        self.refresh_expiry_ms = settings.JWT_REFRESH_TOKEN_EXPIRY_MS
        self.cookie_secure = settings.JWT_COOKIE_SECURE
        self.access_cookie_name = settings.ACCESS_TOKEN_COOKIE
        self.refresh_cookie_name = settings.REFRESH_TOKEN_COOKIE

    def generate_access_token(self, user_id: str, store_id: Optional[str] = None) -> str:
        return self._build_token(user_id, store_id, self.access_expiry_ms)

    def generate_refresh_token(self, user_id: str, store_id: Optional[str] = None) -> str:
        return self._build_token(user_id, store_id, self.refresh_expiry_ms)

    def extract_user_id(self, token: str) -> str:
        payload = self.decode_token(token)
        return str(payload.get("sub"))

    def extract_store_id(self, token: str) -> Optional[str]:
        payload = self.decode_token(token)
        return payload.get("store_id")

    def is_valid(self, token: str) -> bool:
        try:
            self.decode_token(token)
            return True
        except Exception:
            return False

    def decode_token(self, token: str) -> Dict[str, Any]:
        return jwt.decode(
            token,
            self.secret,
            algorithms=[self.algorithm],
        )

    def set_auth_cookies(
        self,
        response: Response,
        access_token: str,
        refresh_token: str,
    ) -> None:
        access_max_age = max(1, self.access_expiry_ms // 1000)
        refresh_max_age = max(1, self.refresh_expiry_ms // 1000)
        samesite = "none" if self.cookie_secure else "lax"

        response.set_cookie(
            key=self.access_cookie_name,
            value=access_token,
            max_age=access_max_age,
            expires=access_max_age,
            path="/",
            httponly=True,
            secure=self.cookie_secure,
            samesite=samesite,
        )
        response.set_cookie(
            key=self.refresh_cookie_name,
            value=refresh_token,
            max_age=refresh_max_age,
            expires=refresh_max_age,
            path="/",
            httponly=True,
            secure=self.cookie_secure,
            samesite=samesite,
        )

    def clear_auth_cookies(self, response: Response) -> None:
        samesite = "none" if self.cookie_secure else "lax"
        response.delete_cookie(
            key=self.access_cookie_name,
            path="/",
            httponly=True,
            secure=self.cookie_secure,
            samesite=samesite,
        )
        response.delete_cookie(
            key=self.refresh_cookie_name,
            path="/",
            httponly=True,
            secure=self.cookie_secure,
            samesite=samesite,
        )

    def _build_token(self, user_id: str, store_id: Optional[str], expiry_ms: int) -> str:
        now = datetime.now(timezone.utc)
        expires_at = now + timedelta(milliseconds=expiry_ms)
        payload: Dict[str, Any] = {
            "sub": str(user_id),
            "iat": int(now.timestamp()),
            "exp": int(expires_at.timestamp()),
        }
        if store_id:
            payload["store_id"] = str(store_id)
        return jwt.encode(payload, self.secret, algorithm=self.algorithm)


jwt_token_provider = JwtTokenProvider()
