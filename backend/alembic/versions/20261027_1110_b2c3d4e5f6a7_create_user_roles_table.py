from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


revision: str = 'b2c3d4e5f6a7'
down_revision: Union[str, None] = 'a1b2c3d4e5f6'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.create_table(
        'user_roles',
        sa.Column('id', sa.Uuid(), nullable=False),
        sa.Column('user_id', sa.Uuid(), sa.ForeignKey('users.id', ondelete='CASCADE'), nullable=False),
        sa.Column('role', sa.String(length=50), server_default='USER', nullable=False),
        sa.Column('created_by', sa.String(length=36), nullable=True),
        sa.Column('created_on', sa.DateTime(timezone=True), nullable=False),
        sa.Column('last_modified_by', sa.String(length=36), nullable=True),
        sa.Column('last_modified_on', sa.DateTime(timezone=True), nullable=False),
        sa.Column('deleted_by', sa.String(length=36), nullable=True),
        sa.Column('deleted_on', sa.DateTime(timezone=True), nullable=True),
        sa.Column('active', sa.Boolean(), server_default=sa.true(), nullable=False),
        sa.Column('deleted', sa.Boolean(), server_default=sa.false(), nullable=False),
        sa.Column('remark', sa.Text(), nullable=True),
        sa.Column('version', sa.BigInteger(), server_default='1', nullable=False),
        sa.PrimaryKeyConstraint('id'),
    )
    op.create_index(op.f('ix_user_roles_user_id'), 'user_roles', ['user_id'], unique=False)
    op.create_index(op.f('ix_user_roles_role'), 'user_roles', ['role'], unique=False)

    op.execute(
        "INSERT INTO user_roles (id, user_id, role, created_on, last_modified_on, active, deleted, version) "
        "SELECT gen_random_uuid(), id, COALESCE(role, 'USER'), created_on, last_modified_on, active, deleted, version "
        "FROM users"
    )

    op.drop_column('users', 'role')


def downgrade() -> None:
    op.add_column('users', sa.Column('role', sa.String(length=50), server_default='USER', nullable=False))
    op.execute(
        "UPDATE users u SET role = COALESCE((SELECT ur.role FROM user_roles ur WHERE ur.user_id = u.id AND ur.deleted = false ORDER BY ur.created_on DESC LIMIT 1), 'USER')"
    )
    op.drop_index(op.f('ix_user_roles_role'), table_name='user_roles')
    op.drop_index(op.f('ix_user_roles_user_id'), table_name='user_roles')
    op.drop_table('user_roles')
