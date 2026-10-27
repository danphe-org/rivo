from abc import ABC, abstractmethod
from datetime import datetime, timezone
from typing import Any, Generic, Optional, TypeVar

Rq = TypeVar("Rq")
Rp = TypeVar("Rp")


class UseCase(Generic[Rq, Rp], ABC):

    def execute(self, request: Rq) -> Rp:
        self.validate(request)
        return self.process(request)

    def validate(self, request: Rq) -> None:
        pass

    def prepare_domain(
        self,
        entity: Any,
        is_create: Optional[bool] = True,
        actor: Optional[str] = None,
        created_by: Optional[str] = None,
        updated_by: Optional[str] = None,
        modified_by: Optional[str] = None,
        remark: Optional[str] = None,
        **kwargs: Any,
    ) -> Any:
        now = datetime.now(timezone.utc)
        effective_create_actor = created_by if created_by is not None else actor
        effective_update_actor = (
            updated_by
            if updated_by is not None
            else (modified_by if modified_by is not None else (actor or created_by))
        )

        if is_create is None:
            if hasattr(entity, "created_on"):
                is_create = getattr(entity, "created_on", None) is None
            elif isinstance(entity, dict):
                is_create = "created_on" not in entity
            else:
                is_create = True

        if isinstance(entity, dict):
            if is_create:
                entity.setdefault("created_on", now)
                if effective_create_actor is not None:
                    entity.setdefault("created_by", str(effective_create_actor))
                entity["last_modified_on"] = now
                if effective_update_actor is not None:
                    entity["last_modified_by"] = str(effective_update_actor)
                entity.setdefault("active", True)
                entity.setdefault("deleted", False)
                entity.setdefault("version", 1)
            else:
                entity["last_modified_on"] = now
                if effective_update_actor is not None:
                    entity["last_modified_by"] = str(effective_update_actor)
                curr_version = entity.get("version", 1) or 1
                entity["version"] = curr_version + 1

            if remark is not None:
                entity["remark"] = remark

            for k, v in kwargs.items():
                entity[k] = v

            return entity

        if is_create:
            if hasattr(entity, "created_on"):
                setattr(entity, "created_on", now)
            if hasattr(entity, "created_by") and effective_create_actor is not None:
                setattr(entity, "created_by", str(effective_create_actor))
            if hasattr(entity, "last_modified_on"):
                setattr(entity, "last_modified_on", now)
            if hasattr(entity, "last_modified_by") and effective_update_actor is not None:
                setattr(entity, "last_modified_by", str(effective_update_actor))
            if hasattr(entity, "active") and getattr(entity, "active", None) is None:
                setattr(entity, "active", True)
            if hasattr(entity, "deleted") and getattr(entity, "deleted", None) is None:
                setattr(entity, "deleted", False)
            if hasattr(entity, "version") and getattr(entity, "version", None) is None:
                setattr(entity, "version", 1)
        else:
            if hasattr(entity, "last_modified_on"):
                setattr(entity, "last_modified_on", now)
            if hasattr(entity, "last_modified_by") and effective_update_actor is not None:
                setattr(entity, "last_modified_by", str(effective_update_actor))
            if hasattr(entity, "version"):
                curr_version = getattr(entity, "version", 1) or 1
                setattr(entity, "version", curr_version + 1)

        if remark is not None and hasattr(entity, "remark"):
            setattr(entity, "remark", remark)

        for k, v in kwargs.items():
            if hasattr(entity, k):
                setattr(entity, k, v)

        return entity

    @abstractmethod
    def process(self, request: Rq) -> Rp:
        pass
