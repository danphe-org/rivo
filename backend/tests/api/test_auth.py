import unittest
import uuid
from fastapi.testclient import TestClient

from app.core.config import settings
from app.core.security import hash_password, jwt_token_provider
from app.main import app
from app.modules.users.models import User, UserRole
from app.modules.users.repository import user_repository


class TestAuthFlow(unittest.TestCase):

    def setUp(self):
        self.client = TestClient(app)
        self.unique_suffix = uuid.uuid4().hex[:8]
        self.unique_phone = f"+1{int(self.unique_suffix, 16) % 1000000000:09d}"

    def test_client_register_and_login_flow(self):
        reg_payload = {
            "name": f"Auth Client User {self.unique_suffix}",
            "email": f"auth_client_{self.unique_suffix}@example.com",
            "password": "Password123!",
            "phone": self.unique_phone,
        }
        reg_res = self.client.post("/api/client/auth/register", json=reg_payload)
        self.assertEqual(reg_res.status_code, 201)
        reg_data = reg_res.json()
        self.assertEqual(reg_data["code"], "0")
        self.assertIsNotNone(reg_data["data"]["id"])
        self.assertEqual(reg_data["data"]["role"], "USER")
        self.assertIsNotNone(reg_data["data"]["access_token"])
        self.assertIsNotNone(reg_data["data"]["refresh_token"])
        self.assertIn(settings.ACCESS_TOKEN_COOKIE, reg_res.cookies)
        self.assertIn(settings.REFRESH_TOKEN_COOKIE, reg_res.cookies)

        login_payload = {
            "email": reg_payload["email"],
            "password": "Password123!",
        }
        login_res = self.client.post("/api/client/auth/login", json=login_payload)
        self.assertEqual(login_res.status_code, 200)
        login_data = login_res.json()
        self.assertEqual(login_data["code"], "0")
        self.assertEqual(login_data["data"]["email"], reg_payload["email"])
        self.assertIn(settings.ACCESS_TOKEN_COOKIE, login_res.cookies)
        self.assertIn(settings.REFRESH_TOKEN_COOKIE, login_res.cookies)

        me_res = self.client.get(
            "/api/client/auth/me",
            cookies={settings.ACCESS_TOKEN_COOKIE: login_data["data"]["access_token"]},
        )
        self.assertEqual(me_res.status_code, 200)
        self.assertEqual(me_res.json()["data"]["id"], login_data["data"]["id"])

    def test_login_invalid_credentials_fails(self):
        login_payload = {
            "email": f"nonexistent_{self.unique_suffix}@example.com",
            "password": "WrongPassword123!",
        }
        res = self.client.post("/api/client/auth/login", json=login_payload)
        self.assertEqual(res.status_code, 400)
        self.assertEqual(res.json()["code"], "400")
        self.assertIn("Invalid email or password", res.json()["message"])

    def test_system_login_enforces_admin_role(self):
        client_user_payload = {
            "name": f"Normal User {self.unique_suffix}",
            "email": f"normal_{self.unique_suffix}@example.com",
            "password": "Password123!",
            "phone": self.unique_phone,
        }
        reg_res = self.client.post("/api/client/auth/register", json=client_user_payload)
        self.assertEqual(reg_res.status_code, 201)

        sys_login_res = self.client.post(
            "/api/system/auth/login",
            json={
                "email": client_user_payload["email"],
                "password": "Password123!",
            },
        )
        self.assertEqual(sys_login_res.status_code, 403)
        self.assertEqual(sys_login_res.json()["code"], "403")
        self.assertIn("Administrative privileges required", sys_login_res.json()["message"])

    def test_refresh_token_flow(self):
        user_email = f"refresh_user_{self.unique_suffix}@example.com"
        reg_payload = {
            "name": f"Refresh User {self.unique_suffix}",
            "email": user_email,
            "password": "Password123!",
            "phone": self.unique_phone,
        }
        reg_res = self.client.post("/api/client/auth/register", json=reg_payload)
        self.assertEqual(reg_res.status_code, 201)
        refresh_token = reg_res.json()["data"]["refresh_token"]

        ref_res = self.client.post(
            "/api/client/auth/refresh",
            cookies={settings.REFRESH_TOKEN_COOKIE: refresh_token},
        )
        self.assertEqual(ref_res.status_code, 200)
        data = ref_res.json()["data"]
        self.assertIsNotNone(data["access_token"])
        self.assertIsNotNone(data["refresh_token"])
        self.assertIn(settings.ACCESS_TOKEN_COOKIE, ref_res.cookies)

    def test_logout_clears_cookies(self):
        res = self.client.post("/api/client/auth/logout")
        self.assertEqual(res.status_code, 200)
        self.assertEqual(res.json()["code"], "0")
        access_cookie = res.headers.get("set-cookie", "")
        self.assertIn(settings.ACCESS_TOKEN_COOKIE, access_cookie)
        self.assertIn(settings.REFRESH_TOKEN_COOKIE, access_cookie)


if __name__ == "__main__":
    unittest.main()
