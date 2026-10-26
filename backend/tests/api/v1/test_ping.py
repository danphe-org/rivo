import unittest
from fastapi.testclient import TestClient

from app.main import app


class TestPingEndpoints(unittest.TestCase):
    def setUp(self):
        self.client = TestClient(app)

    def test_ping_endpoint_success(self):
        response = self.client.get("/api/v1/ping")
        self.assertEqual(response.status_code, 200)

        data = response.json()
        self.assertEqual(data["code"], "0")
        self.assertEqual(data["message"], "Success")
        self.assertEqual(data["data"], "pong")
        self.assertIn("time", data)
        self.assertIn("X-Process-Time", response.headers)

    def test_health_check_endpoint(self):
        response = self.client.get("/health")
        self.assertEqual(response.status_code, 200)

        data = response.json()
        self.assertEqual(data["code"], "0")
        self.assertEqual(data["message"], "Success")
        self.assertEqual(data["data"]["status"], "healthy")

    def test_openapi_docs_endpoint(self):
        response = self.client.get("/openapi.json")
        self.assertEqual(response.status_code, 200)
        docs = response.json()
        self.assertEqual(docs["info"]["title"], "Rivo")
