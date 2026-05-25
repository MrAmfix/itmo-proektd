#!/bin/bash
set -e

echo "⏳ Waiting for PostgreSQL..."
until pg_isready -h db -U postgres -d aifintes; do
  sleep 2
done

echo "📦 Running migrations..."
alembic upgrade head

echo "🚀 Starting API..."
exec uvicorn app.main:app --host 0.0.0.0 --port 8000
