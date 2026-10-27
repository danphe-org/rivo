"""create users table

Revision ID: cc57d506c584
Revises: 
Create Date: 2026-10-26 09:29:19.567586+00:00

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = 'cc57d506c584'
down_revision: Union[str, None] = None
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.create_table(
        'users',
        sa.Column('id', sa.Uuid(), nullable=False, comment='UUID primary key'),
        sa.Column('name', sa.String(length=100), nullable=False, comment='Full name of the user'),
        sa.Column('email', sa.String(length=255), nullable=False, comment='Unique user email address'),
        sa.Column('phone', sa.String(length=30), nullable=True, comment='Contact phone number'),
        sa.Column('role', sa.String(length=50), server_default='USER', nullable=False, comment='Role assigned to user (e.g. USER, ADMIN)'),
        sa.Column('created_by', sa.String(length=36), nullable=True, comment='Identifier of the user who created this record'),
        sa.Column('created_on', sa.DateTime(timezone=True), nullable=False, comment='Timestamp when record was created'),
        sa.Column('last_modified_by', sa.String(length=36), nullable=True, comment='Identifier of the user who last modified this record'),
        sa.Column('last_modified_on', sa.DateTime(timezone=True), nullable=False, comment='Timestamp when record was last modified'),
        sa.Column('deleted_by', sa.String(length=36), nullable=True, comment='Identifier of the user who soft-deleted this record'),
        sa.Column('deleted_on', sa.DateTime(timezone=True), nullable=True, comment='Timestamp when record was soft-deleted'),
        sa.Column('active', sa.Boolean(), server_default=sa.true(), nullable=False, comment='Flag indicating if the record is active'),
        sa.Column('deleted', sa.Boolean(), server_default=sa.false(), nullable=False, comment='Flag indicating if the record has been soft-deleted'),
        sa.Column('remark', sa.Text(), nullable=True, comment='Optional audit remarks or operation notes'),
        sa.Column('version', sa.BigInteger(), server_default='1', nullable=False, comment='Optimistic locking version counter'),
        sa.PrimaryKeyConstraint('id'),
    )
    op.create_index(op.f('ix_users_email'), 'users', ['email'], unique=True)


def downgrade() -> None:
    op.drop_index(op.f('ix_users_email'), table_name='users')
    op.drop_table('users')
