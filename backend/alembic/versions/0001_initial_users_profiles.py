"""initial: users and user_profiles

Revision ID: 0001
Revises:
Create Date: 2026-05-25

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects import postgresql

revision: str = "0001"
down_revision: Union[str, None] = None
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.create_table(
        "users",
        sa.Column("id", postgresql.UUID(as_uuid=True), primary_key=True, server_default=sa.text("gen_random_uuid()")),
        sa.Column("email", sa.String(255), unique=True, nullable=False, index=True),
        sa.Column("password_hash", sa.String(255), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now()),
    )
    op.create_table(
        "user_profiles",
        sa.Column("id", postgresql.UUID(as_uuid=True), primary_key=True, server_default=sa.text("gen_random_uuid()")),
        sa.Column("user_id", postgresql.UUID(as_uuid=True), sa.ForeignKey("users.id", ondelete="CASCADE"), unique=True, nullable=False),
        sa.Column("gender", sa.String(50), nullable=True),
        sa.Column("age", sa.Integer(), nullable=True),
        sa.Column("weight", sa.Float(), nullable=True),
        sa.Column("height", sa.Float(), nullable=True),
        sa.Column("goal", sa.String(50), nullable=True),
        sa.Column("level", sa.String(50), nullable=True),
        sa.Column("injuries", postgresql.ARRAY(sa.String()), nullable=True),
        sa.Column("equipment", postgresql.ARRAY(sa.String()), nullable=True),
        sa.Column("onboarding_completed", sa.Boolean(), default=False),
        sa.Column("medical_disclaimer_accepted", sa.Boolean(), default=False),
    )


def downgrade() -> None:
    op.drop_table("user_profiles")
    op.drop_table("users")
