import uuid
from typing import List, Optional, Tuple
from sqlalchemy import func, or_, select
from sqlalchemy.orm import Session, joinedload, selectinload

from app.db.session import get_current_session
from app.modules.users.models import User, UserRole
from app.modules.users.schemas import UserFilter


class UserRepository:

    def __init__(self, session_provider=get_current_session) -> None:
        self._session_provider = session_provider

    @property
    def session(self) -> Session:
        return self._session_provider()

    def get_by_id(
        self,
        user_id: uuid.UUID,
        include_deleted: bool = False,
    ) -> Optional[User]:
        stmt = (
            select(User)
            .options(joinedload(User.roles))
            .where(User.id == user_id)
        )
        if not include_deleted:
            stmt = stmt.where(User.deleted.is_(False))
        return self.session.scalars(stmt).unique().first()

    def get_by_email(
        self,
        email: str,
        include_deleted: bool = False,
    ) -> Optional[User]:
        stmt = (
            select(User)
            .options(joinedload(User.roles))
            .where(func.lower(User.email) == email.lower().strip())
        )
        if not include_deleted:
            stmt = stmt.where(User.deleted.is_(False))
        return self.session.scalars(stmt).unique().first()

    def get_by_phone(
        self,
        phone: str,
        include_deleted: bool = False,
    ) -> Optional[User]:
        stmt = (
            select(User)
            .options(joinedload(User.roles))
            .where(User.phone == phone.strip())
        )
        if not include_deleted:
            stmt = stmt.where(User.deleted.is_(False))
        return self.session.scalars(stmt).unique().first()

    def find_conflicts(
        self,
        email: Optional[str] = None,
        phone: Optional[str] = None,
        exclude_id: Optional[uuid.UUID] = None,
    ) -> Tuple[Optional[User], Optional[User]]:
        conditions = []
        if email:
            conditions.append(func.lower(User.email) == email.lower().strip())
        if phone:
            conditions.append(User.phone == phone.strip())
        if not conditions:
            return None, None
        stmt = select(User).where(or_(*conditions))
        if exclude_id:
            stmt = stmt.where(User.id != exclude_id)
        results = list(self.session.scalars(stmt).all())
        email_match = next((u for u in results if email and u.email.lower() == email.lower().strip()), None)
        phone_match = next((u for u in results if phone and u.phone and u.phone.strip() == phone.strip()), None)
        return email_match, phone_match

    def save(self, user: User) -> User:
        db = self.session
        db.add(user)
        db.commit()
        return user

    def search(
        self,
        criteria: UserFilter,
    ) -> Tuple[List[User], int]:
        db = self.session
        stmt = select(User).options(selectinload(User.roles))
        count_stmt = select(func.count(User.id))

        if not criteria.delete:
            stmt = stmt.where(User.deleted.is_(False))
            count_stmt = count_stmt.where(User.deleted.is_(False))

        if criteria.active is not None:
            stmt = stmt.where(User.active.is_(criteria.active))
            count_stmt = count_stmt.where(User.active.is_(criteria.active))

        if criteria.id:
            try:
                parsed_id = uuid.UUID(criteria.id)
                stmt = stmt.where(User.id == parsed_id)
                count_stmt = count_stmt.where(User.id == parsed_id)
            except ValueError:
                return [], 0

        if criteria.ids:
            try:
                parsed_ids = [uuid.UUID(i) for i in criteria.ids]
                stmt = stmt.where(User.id.in_(parsed_ids))
                count_stmt = count_stmt.where(User.id.in_(parsed_ids))
            except ValueError:
                return [], 0

        if criteria.role:
            role_pattern = criteria.role.strip().upper()
            role_filter = (
                (UserRole.role == role_pattern)
                & (UserRole.deleted.is_(False))
                & (UserRole.active.is_(True))
            )
            stmt = stmt.where(User.roles.any(role_filter))
            count_stmt = count_stmt.where(User.roles.any(role_filter))

        if criteria.email:
            stmt = stmt.where(func.lower(User.email) == criteria.email.lower().strip())
            count_stmt = count_stmt.where(func.lower(User.email) == criteria.email.lower().strip())

        if criteria.like_search_parameter:
            search_pattern = criteria.like_search_parameter
            search_clause = or_(
                User.name.ilike(search_pattern),
                User.email.ilike(search_pattern),
                User.phone.ilike(search_pattern),
            )
            stmt = stmt.where(search_clause)
            count_stmt = count_stmt.where(search_clause)

        total_records = db.scalar(count_stmt) or 0

        sort_col = getattr(User, criteria.sortField, User.created_on) if criteria.sortField else User.created_on
        if criteria.sort_direction == "DESC":
            stmt = stmt.order_by(sort_col.desc())
        else:
            stmt = stmt.order_by(sort_col.asc())

        stmt = stmt.offset(criteria.offset).limit(criteria.pageSize)

        results = db.scalars(stmt).all()
        return list(results), total_records

    def soft_delete(
        self,
        user: User,
        deleted_by: Optional[str] = None,
    ) -> User:
        user.soft_delete(deleted_by=deleted_by)
        db = self.session
        db.add(user)
        db.commit()
        return user

    def hard_delete(self, user: User) -> None:
        db = self.session
        db.delete(user)
        db.commit()


user_repository = UserRepository()
