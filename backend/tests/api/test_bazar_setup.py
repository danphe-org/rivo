import unittest
from fastapi.testclient import TestClient

from app.common.pagination import AbstractFilter, Page
from app.main import app


class TestBazarSetup(unittest.TestCase):
    def setUp(self):
        self.client = TestClient(app)

    def test_system_ping(self):
        response = self.client.get("/api/system/ping")
        self.assertEqual(response.status_code, 200)

        data = response.json()
        self.assertEqual(data["code"], "0")
        self.assertEqual(data["message"], "Success")
        self.assertEqual(data["data"], "pong")
        self.assertIn("time", data)
        self.assertIn("X-Process-Time", response.headers)

    def test_client_ping(self):
        response = self.client.get("/api/client/ping")
        self.assertEqual(response.status_code, 200)

        data = response.json()
        self.assertEqual(data["code"], "0")
        self.assertEqual(data["message"], "Success")
        self.assertEqual(data["data"], "pong")
        self.assertIn("time", data)

    def test_v1_ping(self):
        response = self.client.get("/api/v1/ping")
        self.assertEqual(response.status_code, 200)

        data = response.json()
        self.assertEqual(data["code"], "0")
        self.assertEqual(data["message"], "Success")
        self.assertEqual(data["data"], "pong")

    def test_health_check(self):
        response = self.client.get("/health")
        self.assertEqual(response.status_code, 200)

        data = response.json()
        self.assertEqual(data["code"], "0")
        self.assertEqual(data["data"]["status"], "healthy")

    def test_page_schema(self):
        page = Page[str](totalRecords=2, limit=10, currentPage=1, data=["item1", "item2"])
        self.assertEqual(page.totalRecords, 2)
        self.assertEqual(len(page.data), 2)

    def test_abstract_filter(self):
        filt = AbstractFilter(pageNumber=2, pageSize=25, searchParameter="test")
        self.assertEqual(filt.offset, 25)
        self.assertEqual(filt.like_search_parameter, "%test%")
        self.assertEqual(filt.sort_direction, "DESC")
