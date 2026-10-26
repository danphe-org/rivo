import unittest
from fastapi import FastAPI
from fastapi.testclient import TestClient
from pydantic import BaseModel, Field

from app.common.schemas import RestResponse
from app.core.exceptions import (
    AbstractError,
    AbstractExceptionManager,
    AppException,
    ForbiddenException,
    NotFoundException,
    UnauthorizedException,
    register_exception_handlers,
)


class SampleBody(BaseModel):
    name: str = Field(..., min_length=2)
    email: str


def create_test_app() -> FastAPI:
    test_app = FastAPI()
    register_exception_handlers(test_app)

    @test_app.get("/test/success")
    def success():
        return RestResponse.ok({"user_id": "123"})

    @test_app.get("/test/app-exception")
    def app_exc():
        raise AppException("Invalid email or password")

    @test_app.get("/test/app-exception-custom-code")
    def app_exc_custom():
        raise AppException("CUSTOM_CODE", "Custom error description")

    @test_app.get("/test/not-found")
    def not_found():
        raise NotFoundException("Store not found")

    @test_app.get("/test/forbidden")
    def forbidden():
        raise ForbiddenException("Active store context is required")

    @test_app.get("/test/unauthorized")
    def unauthorized():
        raise UnauthorizedException("Authentication required")

    @test_app.get("/test/manager-error")
    def manager_error():
        raise AbstractExceptionManager(AbstractError.DUPLICATE_ERROR)

    @test_app.post("/test/validation")
    def validate(body: SampleBody):
        return RestResponse.ok(body.model_dump())

    @test_app.get("/test/unhandled")
    def unhandled():
        raise RuntimeError("Unexpected database connection crash")

    return test_app


class TestExceptionHandling(unittest.TestCase):
    def setUp(self):
        self.client = TestClient(create_test_app(), raise_server_exceptions=False)

    def test_success_envelope(self):
        response = self.client.get("/test/success")
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(data["code"], "0")
        self.assertEqual(data["message"], "Success")
        self.assertEqual(data["data"], {"user_id": "123"})
        self.assertIn("time", data)

    def test_app_exception(self):
        response = self.client.get("/test/app-exception")
        self.assertEqual(response.status_code, 400)
        data = response.json()
        self.assertEqual(data["code"], "400")
        self.assertEqual(data["message"], "Invalid email or password")
        self.assertIsNone(data["data"])
        self.assertIn("time", data)

    def test_app_exception_custom_code(self):
        response = self.client.get("/test/app-exception-custom-code")
        self.assertEqual(response.status_code, 400)
        data = response.json()
        self.assertEqual(data["code"], "CUSTOM_CODE")
        self.assertEqual(data["message"], "Custom error description")
        self.assertIsNone(data["data"])

    def test_not_found_exception(self):
        response = self.client.get("/test/not-found")
        self.assertEqual(response.status_code, 404)
        data = response.json()
        self.assertEqual(data["code"], "404")
        self.assertEqual(data["message"], "Store not found")
        self.assertIsNone(data["data"])

    def test_forbidden_exception(self):
        response = self.client.get("/test/forbidden")
        self.assertEqual(response.status_code, 403)
        data = response.json()
        self.assertEqual(data["code"], "403")
        self.assertEqual(data["message"], "Active store context is required")
        self.assertIsNone(data["data"])

    def test_unauthorized_exception(self):
        response = self.client.get("/test/unauthorized")
        self.assertEqual(response.status_code, 401)
        data = response.json()
        self.assertEqual(data["code"], "401")
        self.assertEqual(data["message"], "Authentication required")
        self.assertIsNone(data["data"])

    def test_abstract_exception_manager(self):
        response = self.client.get("/test/manager-error")
        self.assertEqual(response.status_code, 400)
        data = response.json()
        self.assertEqual(data["code"], "ABE00002")
        self.assertEqual(data["message"], "Duplicate entry found")
        self.assertIsNone(data["data"])

    def test_validation_error(self):
        response = self.client.post("/test/validation", json={"name": "A"})  # missing email, name too short
        self.assertEqual(response.status_code, 400)
        data = response.json()
        self.assertEqual(data["code"], "400")
        self.assertIn("name:", data["message"])
        self.assertIn("email:", data["message"])
        self.assertIsNone(data["data"])

    def test_unhandled_exception(self):
        response = self.client.get("/test/unhandled")
        self.assertEqual(response.status_code, 500)
        data = response.json()
        self.assertEqual(data["code"], "500")
        self.assertIn("Unexpected database connection crash", data["message"])


if __name__ == "__main__":
    unittest.main()
