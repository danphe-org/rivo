# FastAPI Backend

A professional, production-ready FastAPI backend following a **Domain-Driven Modular Architecture**.

## Architecture & Folder Structure

For a full explanation of the folder structure and module responsibilities, see:
* [**Backend Architecture Guide**](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/md/structure.md)

## Getting Started

### 1. Activate Environment
```bash
cd backend
source .venv/bin/activate
```

### 2. Install Dependencies
```bash
pip install -r requirements.txt
```

### 3. Database Migrations (Alembic)

Alembic automatically reads your PostgreSQL configuration from `.env` and tracks all models registered in `app/db/base.py`.

#### Generate a New Migration
After adding or altering entity models in `app/modules/<feature>/models.py`:
```bash
cd backend
alembic revision --autogenerate -m "describe_your_changes"
```

#### Apply Migrations
```bash
cd backend
alembic upgrade head
```

#### Rollback Migration
```bash
cd backend
alembic downgrade -1
```

#### Inspect Current Version
```bash
cd backend
alembic current
alembic history --verbose
```
