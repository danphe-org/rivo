"""Application configuration loaded dynamically from .env.

Provides typed property access and dynamic key-value lookup without
hardcoded imports or static class bloat.
"""
import json
import os
from pathlib import Path
from typing import Any, Dict, List, Optional, Union
from sqlalchemy import URL


class Settings:
    """Dynamic Environment and Configuration provider matching bazar-backend."""

    def __init__(self, env_path: Optional[Union[str, Path]] = None) -> None:
        self._env_file_path: Optional[Path] = self._resolve_env_file(env_path)
        self._properties: Dict[str, str] = {}
        self.load()

    @staticmethod
    def _resolve_env_file(configured_path: Optional[Union[str, Path]] = None) -> Optional[Path]:
        """Finds .env in the configured path, current dir, or parent dirs."""
        if configured_path and Path(configured_path).exists():
            return Path(configured_path).resolve()

        # Check current working directory
        current = Path(".env").resolve()
        if current.exists():
            return current

        # Search up parent directories
        search_dir = Path.cwd().resolve()
        for _ in range(5):
            candidate = search_dir / ".env"
            if candidate.exists():
                return candidate.resolve()
            backend_candidate = search_dir / "backend" / ".env"
            if backend_candidate.exists():
                return backend_candidate.resolve()
            if search_dir.parent == search_dir:
                break
            search_dir = search_dir.parent

        return None

    def load(self) -> None:
        """Parses key-value pairs from .env and populates environment."""
        self._properties.clear()

        # First load OS environment variables
        for k, v in os.environ.items():
            self._properties[k] = v

        if not self._env_file_path or not self._env_file_path.exists():
            return

        with open(self._env_file_path, "r", encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if not line or line.startswith("#") or "=" not in line:
                    continue

                key, val = line.split("=", 1)
                key = key.strip()
                val = val.strip().strip('"').strip("'")

                self._properties[key] = val
                if key not in os.environ:
                    os.environ[key] = val

    def get(self, key: str, default: Optional[Any] = None) -> Any:
        return self._properties.get(key, os.environ.get(key, default))

    def require(self, key: str) -> str:
        val = self.get(key)
        if val is None or str(val).strip() == "":
            raise KeyError(f"Required configuration key '{key}' is missing from .env")
        return str(val).strip()

    def get_int(self, key: str, default: int = 0) -> int:
        val = self.get(key)
        try:
            return int(str(val).strip())
        except (ValueError, TypeError):
            return default

    def get_bool(self, key: str, default: bool = False) -> bool:
        val = self.get(key)
        if val is None:
            return default
        return str(val).strip().lower() in ("true", "1", "yes", "on")

    def get_list(self, key: str, default: Optional[List[str]] = None) -> List[str]:
        val = self.get(key)
        if not val:
            return default or []
        s = str(val).strip()
        if s.startswith("[") and s.endswith("]"):
            try:
                return json.loads(s)
            except Exception:
                pass
        return [part.strip() for part in s.split(",") if part.strip()]

    # Attribute-style access: config.DB_HOST, config.PROJECT_NAME, etc.
    def __getattr__(self, name: str) -> Any:
        if name in self._properties:
            return self._properties[name]
        if name in os.environ:
            return os.environ[name]
        upper_name = name.upper()
        if upper_name in self._properties:
            return self._properties[upper_name]
        return None

    def __getitem__(self, item: str) -> Any:
        val = self.get(item)
        if val is None:
            raise KeyError(item)
        return val

    # Common Application Properties
    @property
    def PROJECT_NAME(self) -> str:
        return self.get("PROJECT_NAME", "Rivo")

    @property
    def ENVIRONMENT(self) -> str:
        return self.get("ENVIRONMENT", "development")

    @property
    def DEBUG(self) -> bool:
        return self.get_bool("DEBUG", True)

    @property
    def API_V1_STR(self) -> str:
        return self.get("API_V1_STR", "/api/v1")

    @property
    def HOST(self) -> str:
        return self.get("HOST", "0.0.0.0")

    @property
    def PORT(self) -> int:
        return self.get_int("PORT", 8000)

    @property
    def CORS_ORIGINS(self) -> List[str]:
        return self.get_list("CORS_ORIGINS", ["http://localhost:3000", "http://localhost:5173", "http://localhost:8000"])

    # Database Properties
    @property
    def DB_HOST(self) -> str:
        return self.get("DB_HOST", "89.117.56.195")

    @property
    def DB_PORT(self) -> int:
        return self.get_int("DB_PORT", 2345)

    @property
    def DB_NAME(self) -> str:
        return self.get("DB_NAME", "danphe_catering")

    @property
    def DB_USER(self) -> str:
        return self.get("DB_USER", self.get("DB_USERNAME", "danphe"))

    @property
    def DB_PASSWORD(self) -> str:
        return self.get("DB_PASSWORD", "Asd@78qwe")

    @property
    def SQLALCHEMY_DATABASE_URL(self) -> URL:
        """Constructs the SQLAlchemy connection URL with proper credential encoding."""
        return URL.create(
            drivername="postgresql+psycopg2",
            username=self.DB_USER,
            password=self.DB_PASSWORD,
            host=self.DB_HOST,
            port=self.DB_PORT,
            database=self.DB_NAME,
        )


# Singleton instances for project-wide use
settings = Settings()
config = settings

__all__ = ["Settings", "settings", "config"]
