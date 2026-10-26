import unittest
from sqlalchemy import text
from sqlalchemy.orm import Session
from fastapi.testclient import TestClient

from app.core import config, settings
from app.core.database import SessionLocal, check_db_connection, engine
from app.db.session import get_db
from app.main import app


class TestDatabaseSetup(unittest.TestCase):
    def test_settings_loaded_from_env(self):
        self.assertIsNotNone(settings.PROJECT_NAME)
        self.assertIsNotNone(settings.SQLALCHEMY_DATABASE_URL)
        self.assertIs(config, settings)
        self.assertIsInstance(settings.CORS_ORIGINS, list)

    def test_engine_connectivity(self):
        with engine.connect() as conn:
            result = conn.execute(text("SELECT 1")).scalar()
            self.assertEqual(result, 1)

    def test_check_db_connection_function(self):
        self.assertTrue(check_db_connection())

    def test_session_lifecycle(self):
        session = SessionLocal()
        try:
            self.assertIsInstance(session, Session)
            val = session.execute(text("SELECT 100")).scalar()
            self.assertEqual(val, 100)
        finally:
            session.close()

    def test_get_db_dependency(self):
        gen = get_db()
        db = next(gen)
        self.assertIsInstance(db, Session)
        val = db.execute(text("SELECT 42")).scalar()
        self.assertEqual(val, 42)
        with self.assertRaises(StopIteration):
            next(gen)

    def test_health_endpoint_reports_connected(self):
        client = TestClient(app)
        response = client.get("/health")
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(data["code"], "0")
        self.assertEqual(data["data"]["database"], "connected")
