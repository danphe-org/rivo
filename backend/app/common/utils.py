from datetime import datetime, timezone


class DateUtils:
    """Date utility functions matching the bazar-backend conventions."""

    @staticmethod
    def now() -> str:
        """Returns the current local ISO timestamp string (matching LocalDateTime.now().toString())."""
        return datetime.now().isoformat()

    @staticmethod
    def now_utc() -> str:
        """Returns current UTC ISO timestamp string."""
        return datetime.now(timezone.utc).isoformat()

    @staticmethod
    def now_formatted(pattern: str = "%Y-%m-%d %H:%M:%S") -> str:
        """Returns formatted current datetime string."""
        return datetime.now().strftime(pattern)
