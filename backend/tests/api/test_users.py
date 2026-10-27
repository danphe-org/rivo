import unittest
import uuid
from fastapi.testclient import TestClient

from app.core.security import verify_password
from app.main import app
from app.modules.users.repository import user_repository


class TestUserCrud(unittest.TestCase):

    def setUp(self):
        self.client = TestClient(app)
        self.unique_suffix = uuid.uuid4().hex[:8]
        self.unique_phone = f"+1{int(self.unique_suffix, 16) % 1000000000:09d}"

    def test_create_user_success(self):
        payload = {
            "name": f"Test User {self.unique_suffix}",
            "email": f"test_{self.unique_suffix}@example.com",
            "phone": self.unique_phone,
            "role": "USER",
        }
        response = self.client.post("/api/system/users", json=payload)
        self.assertEqual(response.status_code, 201)

        data = response.json()
        self.assertEqual(data["code"], "0")
        self.assertEqual(data["message"], "User created successfully")
        self.assertIsNotNone(data["data"]["id"])
        self.assertEqual(data["data"]["email"], payload["email"])
        self.assertEqual(data["data"]["name"], payload["name"])
        self.assertEqual(data["data"]["role"], "USER")
        self.assertTrue(data["data"]["active"])
        self.assertFalse(data["data"]["deleted"])
        self.assertIsNotNone(data["data"]["created_on"])

    def test_create_user_duplicate_email_fails(self):
        payload = {
            "name": f"User One {self.unique_suffix}",
            "email": f"dup_{self.unique_suffix}@example.com",
            "phone": self.unique_phone,
            "role": "USER",
        }
        res1 = self.client.post("/api/system/users", json=payload)
        self.assertEqual(res1.status_code, 201)

        res2 = self.client.post("/api/system/users", json=payload)
        self.assertEqual(res2.status_code, 400)
        err = res2.json()
        self.assertEqual(err["code"], "400")
        self.assertIn("already exists", err["message"])

    def test_create_user_duplicate_phone_fails(self):
        payload1 = {
            "name": f"User Phone A {self.unique_suffix}",
            "email": f"dup_p1_{self.unique_suffix}@example.com",
            "phone": self.unique_phone,
            "role": "USER",
        }
        res1 = self.client.post("/api/system/users", json=payload1)
        self.assertEqual(res1.status_code, 201)

        payload2 = {
            "name": f"User Phone B {self.unique_suffix}",
            "email": f"dup_p2_{self.unique_suffix}@example.com",
            "phone": self.unique_phone,
            "role": "USER",
        }
        res2 = self.client.post("/api/system/users", json=payload2)
        self.assertEqual(res2.status_code, 400)
        err = res2.json()
        self.assertEqual(err["code"], "400")
        self.assertIn("A user with this phone number already exists", err["message"])

    def test_create_user_validation_errors(self):
        res1 = self.client.post(
            "/api/system/users",
            json={"name": "   ", "email": f"valid_{self.unique_suffix}@example.com"},
        )
        self.assertEqual(res1.status_code, 400)

        res2 = self.client.post(
            "/api/system/users",
            json={"name": "Valid Name", "email": "not-an-email"},
        )
        self.assertEqual(res2.status_code, 400)

    def test_get_user_by_id(self):
        create_res = self.client.post(
            "/api/system/users",
            json={
                "name": f"Get User {self.unique_suffix}",
                "email": f"get_{self.unique_suffix}@example.com",
                "phone": self.unique_phone,
            },
        )
        self.assertEqual(create_res.status_code, 201)
        user_id = create_res.json()["data"]["id"]

        get_res = self.client.get(f"/api/system/users/{user_id}")
        self.assertEqual(get_res.status_code, 200)
        data = get_res.json()["data"]
        self.assertEqual(data["id"], user_id)
        self.assertEqual(data["email"], f"get_{self.unique_suffix}@example.com")

    def test_get_user_not_found(self):
        random_id = str(uuid.uuid4())
        response = self.client.get(f"/api/system/users/{random_id}")
        self.assertEqual(response.status_code, 404)
        err = response.json()
        self.assertEqual(err["code"], "404")

    def test_update_user_success(self):
        create_res = self.client.post(
            "/api/system/users",
            json={
                "name": f"Orig User {self.unique_suffix}",
                "email": f"orig_{self.unique_suffix}@example.com",
                "role": "USER",
            },
        )
        self.assertEqual(create_res.status_code, 201)
        user_id = create_res.json()["data"]["id"]

        update_payload = {
            "name": f"Updated Name {self.unique_suffix}",
            "role": "ADMIN",
            "phone": self.unique_phone,
        }
        update_res = self.client.put(f"/api/system/users/{user_id}", json=update_payload)
        self.assertEqual(update_res.status_code, 200)

        data = update_res.json()["data"]
        self.assertEqual(data["name"], update_payload["name"])
        self.assertEqual(data["role"], "ADMIN")
        self.assertEqual(data["phone"], self.unique_phone)
        self.assertEqual(data["email"], f"orig_{self.unique_suffix}@example.com")

    def test_update_user_duplicate_email_conflict(self):
        res1 = self.client.post(
            "/api/system/users",
            json={
                "name": f"User A {self.unique_suffix}",
                "email": f"user_a_{self.unique_suffix}@example.com",
            },
        )
        self.assertEqual(res1.status_code, 201)

        res2 = self.client.post(
            "/api/system/users",
            json={
                "name": f"User B {self.unique_suffix}",
                "email": f"user_b_{self.unique_suffix}@example.com",
            },
        )
        self.assertEqual(res2.status_code, 201)
        user2_id = res2.json()["data"]["id"]

        update_res = self.client.put(
            f"/api/system/users/{user2_id}",
            json={"email": f"user_a_{self.unique_suffix}@example.com"},
        )
        self.assertEqual(update_res.status_code, 400)
        self.assertIn("already exists", update_res.json()["message"])

    def test_delete_user_soft_delete(self):
        create_res = self.client.post(
            "/api/system/users",
            json={
                "name": f"Delete User {self.unique_suffix}",
                "email": f"del_{self.unique_suffix}@example.com",
            },
        )
        self.assertEqual(create_res.status_code, 201)
        user_id = create_res.json()["data"]["id"]

        del_res = self.client.delete(f"/api/system/users/{user_id}")
        self.assertEqual(del_res.status_code, 200)
        self.assertEqual(del_res.json()["message"], "User deleted successfully")

        get_res = self.client.get(f"/api/system/users/{user_id}")
        self.assertEqual(get_res.status_code, 404)

    def test_search_and_pagination(self):
        prefix = f"search_{self.unique_suffix}"
        for i in range(3):
            self.client.post(
                "/api/system/users",
                json={
                    "name": f"Searchable {prefix}_{i}",
                    "email": f"{prefix}_{i}@example.com",
                    "role": "ADMIN" if i == 0 else "USER",
                },
            )

        search_payload = {
            "pageNumber": 1,
            "pageSize": 2,
            "searchParameter": prefix,
            "sortField": "created_on",
            "sortOrder": "ASC",
        }
        search_res = self.client.post("/api/system/users/search", json=search_payload)
        self.assertEqual(search_res.status_code, 200)
        data = search_res.json()["data"]
        self.assertEqual(data["totalRecords"], 3)
        self.assertEqual(len(data["data"]), 2)
        self.assertEqual(data["currentPage"], 1)
        self.assertEqual(data["limit"], 2)

        role_search = self.client.post(
            "/api/system/users/search",
            json={
                "pageNumber": 1,
                "pageSize": 10,
                "searchParameter": prefix,
                "role": "ADMIN",
            },
        )
        self.assertEqual(role_search.status_code, 200)
        role_data = role_search.json()["data"]
        self.assertEqual(role_data["totalRecords"], 1)
        self.assertEqual(role_data["data"][0]["role"], "ADMIN")

        list_payload = {"searchParameter": prefix, "pageSize": 5}
        post_list_res = self.client.post("/api/system/users/list", json=list_payload)
        self.assertEqual(post_list_res.status_code, 200)
        self.assertEqual(post_list_res.json()["data"]["totalRecords"], 3)

        get_list_res = self.client.request("GET", "/api/system/users", json=list_payload)
        self.assertEqual(get_list_res.status_code, 200)
        self.assertEqual(get_list_res.json()["data"]["totalRecords"], 3)

    def test_client_user_registration_success(self):
        payload = {
            "name": f"Registered User {self.unique_suffix}",
            "email": f"reg_{self.unique_suffix}@example.com",
            "password": "SecretPassword123!",
            "phone": self.unique_phone,
            "role": "USER",
        }
        response = self.client.post("/api/client/users/register", json=payload)
        self.assertEqual(response.status_code, 201)

        data = response.json()
        self.assertEqual(data["code"], "0")
        self.assertEqual(data["message"], "User registered successfully")
        self.assertIsNotNone(data["data"]["id"])
        self.assertEqual(data["data"]["email"], payload["email"])
        self.assertEqual(data["data"]["name"], payload["name"])
        self.assertEqual(data["data"]["phone"], payload["phone"])
        self.assertEqual(data["data"]["role"], "USER")
        self.assertTrue(data["data"]["active"])
        self.assertFalse(data["data"]["deleted"])
        self.assertIsNotNone(data["data"]["created_on"])
        self.assertEqual(data["data"]["created_by"], "SELF_REGISTRATION")
        self.assertEqual(data["data"]["version"], 1)
        self.assertNotIn("password", data["data"])

        db_user = user_repository.get_by_id(uuid.UUID(data["data"]["id"]))
        self.assertIsNotNone(db_user)
        self.assertNotEqual(db_user.password, "SecretPassword123!")
        self.assertTrue(verify_password("SecretPassword123!", db_user.password))

    def test_client_user_registration_short_password_fails(self):
        payload = {
            "name": f"Short Pw {self.unique_suffix}",
            "email": f"shortpw_{self.unique_suffix}@example.com",
            "password": "123",
        }
        response = self.client.post("/api/client/users/register", json=payload)
        self.assertEqual(response.status_code, 400)
        err = response.json()
        self.assertEqual(err["code"], "400")
        self.assertIn("at least 8 characters", err["message"])

    def test_client_user_registration_strict_password_rules(self):
        invalid_passwords = [
            ("lowercaseonly123!", "uppercase letter"),
            ("UPPERCASEONLY123!", "lowercase letter"),
            ("NoNumbersHere!@#", "number"),
            ("NoSpecialChar123", "special character"),
            ("Has Space123!", "whitespace"),
        ]
        for pw, err_fragment in invalid_passwords:
            payload = {
                "name": f"Strict Pw {self.unique_suffix}",
                "email": f"strict_{uuid.uuid4().hex[:6]}@example.com",
                "password": pw,
            }
            res = self.client.post("/api/client/users/register", json=payload)
            self.assertEqual(res.status_code, 400)
            self.assertIn(err_fragment, res.json()["message"])

    def test_client_user_registration_duplicate_email(self):
        payload = {
            "name": f"Reg A {self.unique_suffix}",
            "email": f"regdup_{self.unique_suffix}@example.com",
            "password": "SecretPassword123!",
            "phone": self.unique_phone,
        }
        res1 = self.client.post("/api/client/users/register", json=payload)
        self.assertEqual(res1.status_code, 201)

        res2 = self.client.post("/api/client/users/register", json=payload)
        self.assertEqual(res2.status_code, 400)
        err = res2.json()
        self.assertEqual(err["code"], "400")
        self.assertIn("already exists", err["message"])

    def test_client_user_registration_duplicate_phone(self):
        payload1 = {
            "name": f"Reg Phone A {self.unique_suffix}",
            "email": f"reg_pa_{self.unique_suffix}@example.com",
            "password": "SecretPassword123!",
            "phone": self.unique_phone,
        }
        res1 = self.client.post("/api/client/users/register", json=payload1)
        self.assertEqual(res1.status_code, 201)

        payload2 = {
            "name": f"Reg Phone B {self.unique_suffix}",
            "email": f"reg_pb_{self.unique_suffix}@example.com",
            "password": "SecretPassword123!",
            "phone": self.unique_phone,
        }
        res2 = self.client.post("/api/client/users/register", json=payload2)
        self.assertEqual(res2.status_code, 400)
        err = res2.json()
        self.assertEqual(err["code"], "400")
        self.assertIn("A user with this phone number already exists", err["message"])

    def test_client_user_registration_invalid_phone(self):
        response = self.client.post(
            "/api/client/users/register",
            json={
                "name": f"Bad Phone {self.unique_suffix}",
                "email": f"badphone_{self.unique_suffix}@example.com",
                "phone": "invalid-phone",
            },
        )
        self.assertEqual(response.status_code, 400)
        err = response.json()
        self.assertEqual(err["code"], "400")
        self.assertIn("Invalid phone number format", err["message"])

    def test_client_user_registration_privilege_escalation_prohibited(self):
        response = self.client.post(
            "/api/client/users/register",
            json={
                "name": f"Hacker {self.unique_suffix}",
                "email": f"hacker_{self.unique_suffix}@example.com",
                "role": "ADMIN",
            },
        )
        self.assertEqual(response.status_code, 400)
        err = response.json()
        self.assertEqual(err["code"], "400")
        self.assertIn("Self-registration is limited to USER or CUSTOMER roles", err["message"])


if __name__ == "__main__":
    unittest.main()
