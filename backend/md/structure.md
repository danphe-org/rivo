# Backend Architecture & Folder Structure Guide

This document provides a comprehensive overview of the backend design, folder organization, and module responsibilities for this FastAPI project.

---

## 1. Architectural Pattern: Domain-Driven Modular Design

This project follows a **Domain-Driven Modular Architecture** (also known as feature-based or vertical slice architecture).

### Why Modular Design?
* **High Cohesion, Low Coupling**: Each domain feature (e.g., `auth`, `users`) encapsulates its own routes, schemas, models, business logic, and database operations.
* **Scalability**: Adding a new feature involves creating a new folder inside `app/modules/` without having to touch dozens of disconnected folders across the project.
* **Separation of Concerns**: Clear separation between HTTP routing (`router.py`), data validation (`schemas.py`), business logic (`service.py`), and data access (`repository.py`).

---

## 2. Directory Tree Overview

```text
backend/
├── .env.example              # Environment variables template
├── .gitignore                # Git ignore patterns (venv, pycache, env)
├── alembic.ini               # Alembic migration configuration
├── Dockerfile                # Container build specification
├── docker-compose.yml        # Development services (PostgreSQL, Redis, App)
├── requirements.txt          # Python dependencies
├── .venv/                    # Python virtual environment (ignored in git)
│
├── alembic/                  # Database migration scripts
│   ├── env.py                # Alembic migration execution context
│   ├── script.py.mako        # Migration template
│   └── versions/             # Migration version scripts (.gitkeep)
│
├── app/                      # Application core code
│   ├── __init__.py
│   ├── main.py               # FastAPI entry point & application factory
│   │
│   ├── core/                 # Global configuration & infrastructure
│   │   ├── __init__.py
│   │   ├── config.py         # Application settings (Pydantic BaseSettings)
│   │   ├── database.py       # DB engine, session maker, connections
│   │   ├── security.py       # Cryptography, password hashing, JWT tokens
│   │   ├── exceptions.py     # Global custom exceptions and error handlers
│   │   └── logging.py        # Structured logging configuration
│   │
│   ├── common/               # Shared cross-cutting components
│   │   ├── __init__.py
│   │   ├── models.py         # Base ORM model (ID, timestamps mixin)
│   │   ├── schemas.py        # Generic API responses (ApiResponse, ErrorResponse)
│   │   ├── pagination.py     # Pagination query params and page response schemas
│   │   └── utils.py          # Shared utility helper functions
│   │
│   ├── api/                  # API routing & versioning
│   │   ├── __init__.py
│   │   ├── deps.py           # Shared API dependencies (get_db, current_user)
│   │   └── v1/               # API version 1
│   │       ├── __init__.py
│   │       └── router.py     # Aggregator combining all module routers for v1
│   │
│   ├── modules/              # Domain/Feature modules
│   │   ├── __init__.py
│   │   │
│   │   ├── auth/             # Authentication & Authorization Module
│   │   │   ├── __init__.py
│   │   │   ├── router.py     # Auth HTTP endpoints (/login, /register, /refresh)
│   │   │   ├── schemas.py    # Request/Response schemas (Token, LoginPayload)
│   │   │   ├── models.py     # Auth database models (e.g. RefreshTokens)
│   │   │   ├── service.py    # Auth business logic (verify passwords, issue tokens)
│   │   │   ├── repository.py # Auth database queries
│   │   │   └── dependencies.py# Auth-specific route dependencies
│   │   │
│   │   └── users/            # Users Domain Module
│   │       ├── __init__.py
│   │       ├── router.py     # User endpoints (CRUD, /me, status updates)
│   │       ├── schemas.py    # User schemas (UserCreate, UserUpdate, UserRead)
│   │       ├── models.py     # User SQLAlchemy ORM entity
│   │       ├── service.py    # User business rules and validation logic
│   │       ├── repository.py # User database access layer (queries)
│   │       └── dependencies.py# User-specific route dependencies (get_user_by_id)
│   │
│   └── db/                   # Database metadata and session helpers
│       ├── __init__.py
│       ├── base.py           # Imports all models to expose Base.metadata to Alembic
│       └── session.py        # Database session lifecycle
│
└── tests/                    # Automated test suite
    ├── __init__.py
    ├── conftest.py           # Pytest fixtures (test client, test DB session)
    ├── api/                  # API endpoint integration tests
    │   ├── __init__.py
    │   └── v1/
    │       └── __init__.py
    └── modules/              # Unit & module-specific tests
        ├── __init__.py
        ├── auth/
        │   ├── __init__.py
        │   └── test_auth.py
        └── users/
            ├── __init__.py
            └── test_users.py
```

---

## 3. Detailed Folder & Module Responsibilities

### `app/main.py`
The application factory and entry point.
* Initializes the `FastAPI` instance.
* Attaches global middlewares (CORS, Request ID, Logging, GZip).
* Registers global exception handlers.
* Includes the top-level API router (`app/api/v1/router.py`).
* Configures application lifespan events (startup / shutdown DB connections).

---

### `app/core/` (Core Infrastructure)
Contains non-domain-specific infrastructure components required across the application.

* [`config.py`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/core/config.py): Pydantic `BaseSettings` reading environment variables (database credentials, JWT secret keys, API host, debug mode).
* [`database.py`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/core/database.py): Engine creation (`create_async_engine` or `create_engine`) and session makers.
* [`security.py`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/core/security.py): Password hashing (Passlib/Bcrypt), JWT token generation, and signature verification.
* [`exceptions.py`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/core/exceptions.py): Custom application exceptions (e.g. `EntityNotFoundError`, `PermissionDeniedError`) and their mapping to HTTP responses.
* [`logging.py`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/core/logging.py): Logging formatters, structured logging, and log level configuration.

---

### `app/common/` (Shared Base Classes & Utilities)
Cross-cutting code shared across multiple feature modules.

* [`models.py`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/common/models.py): Declarative base classes and common mixins (e.g., `TimestampMixin` with `created_at` and `updated_at`, UUID primary keys).
* [`schemas.py`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/common/schemas.py): Generic response schemas (e.g., standard API envelope `{"data": ..., "message": "...", "success": true}`).
* [`pagination.py`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/common/pagination.py): Pagination query parameter dependencies (`page`, `limit`) and paginated response models (`items`, `total_count`, `page_number`).
* [`utils.py`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/common/utils.py): Generic utility helpers (date manipulation, string formatters, slug generators).

---

### `app/api/` (API Layer & Versioning)
Responsible for API endpoint discovery and top-level dependency wiring.

* [`deps.py`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/api/deps.py): Global FastAPI dependencies used across routes (e.g., `get_db_session`, `get_current_active_user`).
* [`v1/router.py`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/api/v1/router.py): Aggregates all feature module routers (e.g., `auth_router`, `users_router`) under the `/api/v1` prefix. Adding API v2 in the future simply involves creating an `app/api/v2/` directory without disturbing v1.

---

### `app/modules/` (Domain / Feature Modules)
The core of the modular architecture. Every distinct business feature has its own folder containing the complete vertical slice:

Each module contains:
| File | Responsibility |
| :--- | :--- |
| **`router.py`** | FastAPI endpoint handlers, status codes, route tags, and parameter bindings. |
| **`schemas.py`** | Pydantic DTOs (Data Transfer Objects) for request validation and response serialization. |
| **`models.py`** | SQLAlchemy ORM database models representing tables and relationships. |
| **`service.py`** | Business logic layer: orchestrates database operations, enforces business rules, calls third-party services. |
| **`repository.py`** | Data access layer: raw database operations, queries, filtering, and updates. Keeps database concerns separated from business logic. |
| **`dependencies.py`** | FastAPI `Depends()` specific to this module (e.g., checking if an entity exists by ID from route parameters). |

#### Example Modules Included:
1. **`auth/`**: Registration, token exchange (OAuth2/JWT), login, password resets, refresh tokens.
2. **`users/`**: User account management, profile updates, role assignment, user queries.

---

### `app/db/` (Database Metadata & Sessions)
* [`base.py`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/db/base.py): Imports all module models so that SQLAlchemy's `Base.metadata` knows about every table when Alembic autogenerates migrations.
* [`session.py`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/db/session.py): DB session dependency factory used to yield database sessions per request.

---

### `alembic/` (Database Migrations)
* Managed by Alembic to version-control database schema changes.
* `env.py`: Configured to read the database URL from `app.core.config` and target `app.db.base.Base.metadata`.
* `versions/`: Holds migration scripts generated via `alembic revision --autogenerate`.

---

### `tests/` (Testing Suite)
Mirrors the structure of the application:
* [`conftest.py`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/tests/conftest.py): Pytest fixtures for creating a temporary test database, running migrations, and providing an `httpx.AsyncClient` or `TestClient`.
* `api/`: End-to-end integration tests testing HTTP endpoints.
* `modules/`: Unit tests focusing on services and repositories in isolation.

---

## 4. Request Lifecycle & Data Flow

When an HTTP request reaches the backend, it flows through the layers in this sequence:

```text
1. Client HTTP Request
       │
       ▼
2. app/main.py (Middlewares: CORS, Request ID, Error Handling)
       │
       ▼
3. app/api/v1/router.py (Routing & Versioning)
       │
       ▼
4. app/modules/{feature}/router.py (Endpoint Handler)
       │
       ├──> app/modules/{feature}/dependencies.py (Validation & Authentication)
       │
       ▼
5. app/modules/{feature}/schemas.py (Request Body Validation)
       │
       ▼
6. app/modules/{feature}/service.py (Business Rules & Orchestration)
       │
       ▼
7. app/modules/{feature}/repository.py (Database Queries)
       │
       ▼
8. app/modules/{feature}/models.py (SQLAlchemy ORM Database Entity)
       │
       ▼
9. app/modules/{feature}/schemas.py (Response Serialization)
       │
       ▼
10. HTTP Response returned to Client
```

---

## 5. How to Add a New Feature Module

When creating a new feature (e.g. `products`):

1. **Create the module folder**:
   ```bash
   mkdir app/modules/products
   touch app/modules/products/{__init__.py,router.py,schemas.py,models.py,service.py,repository.py,dependencies.py}
   ```
2. **Define the database model** in `app/modules/products/models.py`.
3. **Register the model** in `app/db/base.py` for Alembic migrations.
4. **Define request/response schemas** in `app/modules/products/schemas.py`.
5. **Implement DB queries** in `app/modules/products/repository.py`.
6. **Implement business rules** in `app/modules/products/service.py`.
7. **Define endpoints** in `app/modules/products/router.py`.
8. **Include the router** in `app/api/v1/router.py`:
   ```python
   from app.modules.products.router import router as products_router
   api_router.include_router(products_router, prefix="/products", tags=["Products"])
   ```
9. **Add tests** in `tests/modules/products/test_products.py`.
