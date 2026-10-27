from datetime import datetime
import unittest
from unittest.mock import MagicMock
import uuid

from app.common.usecase import UseCase
from app.core.exceptions import AppException, NotFoundException
from app.modules.users.models import User
from app.modules.users.schemas import (
    UserCreateDTO,
    UserFilter,
    UserRegisterDTO,
    UserUpdateDTO,
)
from app.modules.users.usecases import (
    CreateUserCommand,
    CreateUserUseCase,
    DeleteUserCommand,
    DeleteUserUseCase,
    GetUserUseCase,
    RegisterUserCommand,
    RegisterUserUseCase,
    SearchUsersUseCase,
    UpdateUserCommand,
    UpdateUserUseCase,
)


class DummyTestUseCase(UseCase[str, str]):

    def __init__(self) -> None:
        self.validation_called = False
        self.process_called = False

    def validate(self, request: str) -> None:
        self.validation_called = True
        if request == "invalid":
            raise AppException("Validation failed")

    def process(self, request: str) -> str:
        self.process_called = True
        return f"processed: {request}"


class TestUseCaseArchitecture(unittest.TestCase):

    def test_base_use_case_lifecycle_success(self):
        use_case = DummyTestUseCase()
        result = use_case.execute("valid_input")
        self.assertTrue(use_case.validation_called)
        self.assertTrue(use_case.process_called)
        self.assertEqual(result, "processed: valid_input")

    def test_base_use_case_lifecycle_validation_failure(self):
        use_case = DummyTestUseCase()
        with self.assertRaises(AppException) as ctx:
            use_case.execute("invalid")
        self.assertTrue(use_case.validation_called)
        self.assertFalse(use_case.process_called)
        self.assertIn("Validation failed", str(ctx.exception))

    def test_prepare_domain_for_model_creation(self):
        use_case = DummyTestUseCase()
        user = User(name="Test", email="test@example.com")
        prepared = use_case.prepare_domain(
            user,
            is_create=True,
            actor="admin_uuid_123",
            remark="Initial creation",
        )
        self.assertEqual(prepared.created_by, "admin_uuid_123")
        self.assertEqual(prepared.last_modified_by, "admin_uuid_123")
        self.assertEqual(prepared.updated_by, "admin_uuid_123")
        self.assertEqual(prepared.modified_by, "admin_uuid_123")
        self.assertIsInstance(prepared.created_on, datetime)
        self.assertIsInstance(prepared.last_modified_on, datetime)
        self.assertEqual(prepared.updated_on, prepared.last_modified_on)
        self.assertTrue(prepared.active)
        self.assertFalse(prepared.deleted)
        self.assertEqual(prepared.version, 1)
        self.assertEqual(prepared.remark, "Initial creation")

    def test_prepare_domain_for_model_update(self):
        use_case = DummyTestUseCase()
        user = User(name="Test", email="test@example.com", created_by="creator_123", version=1)
        initial_modified_on = user.last_modified_on

        prepared = use_case.prepare_domain(
            user,
            is_create=False,
            actor="updater_456",
            remark="Record updated",
        )
        self.assertEqual(prepared.created_by, "creator_123")
        self.assertEqual(prepared.last_modified_by, "updater_456")
        self.assertEqual(prepared.updated_by, "updater_456")
        self.assertEqual(prepared.version, 2)
        self.assertEqual(prepared.remark, "Record updated")
        self.assertGreaterEqual(prepared.last_modified_on, initial_modified_on)

    def test_prepare_domain_for_dict_create_and_update(self):
        use_case = DummyTestUseCase()
        payload = {"name": "Dict Entity"}
        created_dict = use_case.prepare_domain(
            payload,
            is_create=True,
            created_by="user_001",
            remark="Dict creation",
        )
        self.assertIn("created_on", created_dict)
        self.assertEqual(created_dict["created_by"], "user_001")
        self.assertEqual(created_dict["last_modified_by"], "user_001")
        self.assertTrue(created_dict["active"])
        self.assertFalse(created_dict["deleted"])
        self.assertEqual(created_dict["version"], 1)

        updated_dict = use_case.prepare_domain(
            created_dict,
            is_create=False,
            updated_by="user_002",
        )
        self.assertEqual(updated_dict["last_modified_by"], "user_002")
        self.assertEqual(updated_dict["version"], 2)

    def test_register_user_use_case_success_and_prepare_domain(self):
        mock_repo = MagicMock()
        mock_repo.get_by_email.return_value = None
        mock_repo.get_by_phone.return_value = None
        mock_repo.save.side_effect = lambda u: u

        use_case = RegisterUserUseCase(mock_repo)
        cmd = RegisterUserCommand(
            dto=UserRegisterDTO(
                name="John Public",
                email="john.public@example.com",
                phone="+1-555-123-4567",
                role="USER",
            ),
            created_by="REGISTRATION_FLOW",
        )

        response = use_case.execute(cmd)
        self.assertEqual(response.name, "John Public")
        self.assertEqual(response.email, "john.public@example.com")
        self.assertEqual(response.role, "USER")
        self.assertEqual(response.created_by, "REGISTRATION_FLOW")
        self.assertTrue(response.active)
        self.assertFalse(response.deleted)
        self.assertEqual(response.version, 1)
        mock_repo.save.assert_called_once()

    def test_register_user_duplicate_email_fails(self):
        mock_repo = MagicMock()
        mock_repo.get_by_email.return_value = User(
            id=uuid.uuid4(),
            email="existing@example.com",
            name="Existing User",
        )

        use_case = RegisterUserUseCase(mock_repo)
        cmd = RegisterUserCommand(
            dto=UserRegisterDTO(name="New User", email="existing@example.com")
        )

        with self.assertRaises(AppException) as ctx:
            use_case.validate(cmd)
        self.assertIn("already exists", str(ctx.exception))

    def test_register_user_invalid_phone_format_fails(self):
        mock_repo = MagicMock()
        mock_repo.get_by_email.return_value = None

        use_case = RegisterUserUseCase(mock_repo)
        cmd = RegisterUserCommand(
            dto=UserRegisterDTO(
                name="New User",
                email="fresh@example.com",
                phone="abc-invalid",
            )
        )

        with self.assertRaises(AppException) as ctx:
            use_case.validate(cmd)
        self.assertIn("Invalid phone number format", str(ctx.exception))

    def test_register_user_prohibits_admin_privilege_escalation(self):
        mock_repo = MagicMock()
        mock_repo.get_by_email.return_value = None

        use_case = RegisterUserUseCase(mock_repo)
        cmd = RegisterUserCommand(
            dto=UserRegisterDTO(
                name="Escalation Attempter",
                email="attempter@example.com",
                role="ADMIN",
            )
        )

        with self.assertRaises(AppException) as ctx:
            use_case.validate(cmd)
        self.assertIn("Self-registration is limited to USER or CUSTOMER roles", str(ctx.exception))

    def test_register_user_invalid_role_fails(self):
        mock_repo = MagicMock()
        mock_repo.get_by_email.return_value = None

        use_case = RegisterUserUseCase(mock_repo)
        cmd = RegisterUserCommand(
            dto=UserRegisterDTO(
                name="Bad Role User",
                email="badrole@example.com",
                role="SUPER_HACKER",
            )
        )

        with self.assertRaises(AppException) as ctx:
            use_case.validate(cmd)
        self.assertIn("Invalid user role", str(ctx.exception))

    def test_create_user_use_case_validation_fails_on_duplicate(self):
        mock_repo = MagicMock()
        mock_repo.get_by_email.return_value = User(
            id=uuid.uuid4(),
            email="existing@example.com",
            name="Existing",
        )

        use_case = CreateUserUseCase(mock_repo)
        cmd = CreateUserCommand(
            dto=UserCreateDTO(name="New User", email="existing@example.com")
        )

        with self.assertRaises(AppException) as ctx:
            use_case.validate(cmd)
        self.assertIn("already exists", str(ctx.exception))

    def test_get_user_use_case_validation_fails_when_not_found(self):
        mock_repo = MagicMock()
        mock_repo.get_by_id.return_value = None

        use_case = GetUserUseCase(mock_repo)
        random_id = uuid.uuid4()

        with self.assertRaises(NotFoundException) as ctx:
            use_case.validate(random_id)
        self.assertIn("User not found", str(ctx.exception))

    def test_update_user_use_case_validation_fails_when_not_found(self):
        mock_repo = MagicMock()
        mock_repo.get_by_id.return_value = None

        use_case = UpdateUserUseCase(mock_repo)
        random_id = uuid.uuid4()
        cmd = UpdateUserCommand(user_id=random_id, dto=UserUpdateDTO(name="Name"))

        with self.assertRaises(NotFoundException) as ctx:
            use_case.validate(cmd)
        self.assertIn("User not found", str(ctx.exception))

    def test_update_user_use_case_validation_fails_on_duplicate_email(self):
        user_id = uuid.uuid4()
        other_user_id = uuid.uuid4()

        current_user = User(id=user_id, email="current@example.com", name="Current")
        existing_other = User(id=other_user_id, email="other@example.com", name="Other")

        mock_repo = MagicMock()
        mock_repo.get_by_id.return_value = current_user
        mock_repo.get_by_email.return_value = existing_other

        use_case = UpdateUserUseCase(mock_repo)
        cmd = UpdateUserCommand(
            user_id=user_id,
            dto=UserUpdateDTO(email="other@example.com"),
        )

        with self.assertRaises(AppException) as ctx:
            use_case.validate(cmd)
        self.assertIn("already exists", str(ctx.exception))

    def test_delete_user_use_case_validation_fails_when_not_found(self):
        mock_repo = MagicMock()
        mock_repo.get_by_id.return_value = None

        use_case = DeleteUserUseCase(mock_repo)
        random_id = uuid.uuid4()
        cmd = DeleteUserCommand(user_id=random_id)

        with self.assertRaises(NotFoundException) as ctx:
            use_case.validate(cmd)
        self.assertIn("User not found", str(ctx.exception))

    def test_search_users_use_case_validation_invalid_bounds(self):
        mock_repo = MagicMock()
        use_case = SearchUsersUseCase(mock_repo)

        invalid_page = UserFilter.model_construct(pageNumber=0, pageSize=10)
        with self.assertRaises(AppException) as ctx:
            use_case.validate(invalid_page)
        self.assertIn("pageNumber", str(ctx.exception))


if __name__ == "__main__":
    unittest.main()
