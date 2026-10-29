# Project Rules & Coding Standards

This document defines the architectural, design, and coding guidelines for this backend project, adapted directly from the standards established in **Danphe / Bazar Backend** (`bazar_backend`).

All AI agents, engineers, and future modifications **MUST strictly adhere** to these rules on every prompt, session restart, and code change.

---

## 1. Architectural Boundaries & Layer Responsibilities

Dependencies flow strictly from outer to inner layers in a unidirectional pipeline:

```text
[app/api/ (system & client portals)]
                 │
                 ▼
          [app/service/]
                 │
                 ▼
        [app/repository/]
                 │
                 ▼
          [app/common/]
```

### Layer Rules & Forbidden Crossings

1. **`app/common/` (Cross-Cutting Foundation)**
   - **Contains**:
     - Declarative base models and audit mixins: [`Base`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/common/models.py#L24-L26), [`AuditMixin`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/common/models.py#L29-L107), [`BaseModel`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/common/models.py#L110-L162) matching `AuditableModal` and `AuditEntity`.
     - Filter and pagination schemas: [`AbstractFilter`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/common/pagination.py#L22-L52), [`Page<T>`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/common/pagination.py#L13-L19).
     - Base UseCase template: [`UseCase[Rq, Rp]`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/common/usecase.py) enforcing the `execute(request)` -> `validate(request)` -> `process(request)` lifecycle.
     - Standard REST response envelopes and constants: [`ResponseTemplate<T>`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/common/schemas.py#L20-L26), [`RestResponse`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/common/schemas.py#L48-L80), [`ApiConstants`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/common/constants.py#L4-L9).
     - Shared utilities: [`DateUtils`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/common/utils.py#L4-L20).
   - **Strict Rule**: Must **NEVER** import or depend on `repository`, `service`, or `api` layers.

2. **`app/repository/` (or module repositories)**
   - **Contains**: SQLAlchemy database queries, DAO helpers, session queries, and entity-to-domain mapping.
   - **Strict Rule**:
     - Allowed dependency: **`app/common/` only**.
     - Raw database connections, engine sessions, and SQLAlchemy query specifics must remain strictly encapsulated within repositories.
     - Never import `service` or `api` inside repositories.

3. **`app/service/` (or module services and use cases)**
   - **Contains**: Dedicated UseCase classes (`usecase/` or `usecases.py`) and orchestrating service classes (`service.py` or `ServiceImpl.java`).
   - **Strict Rule**:
     - Allowed dependencies: **`app/common/` and `app/repository/`**.
     - Must **NEVER** import from `app/api/` (web layer).
     - Controllers must not contain business logic; all logic belongs in services/UseCases.
     - **Validation Separation Policy (NEVER duplicate validations)**:
       - **Security & Route Authorization**: Handled at the web/security layer via `SecurityConfig` and method security (`@PreAuthorize`). Do NOT check whether the user is authenticated (e.g. `actor == null`) inside UseCase `validate()`.
       - **Request DTOs / Records (`*Dto.java` or schemas)**: Validate all structural, nullability, blank, length, and range constraints directly on the request record using bean validation annotations (`@NotNull`, `@NotBlank`, `@Size(min, max)`, `@Min`, `@Max`, `@Pattern`).
       - **UseCases `validate(request)` Hook**: Do **NOT** re-check authentication or re-validate null, blank, min, or max in `validate()` (re-checking causes unnecessary overhead and slow processing).
       - **What belongs in UseCase `validate(request)`**:
         - **Duplicate / Uniqueness Checks**: Querying repositories to verify unique fields (e.g. code, email, duplicate requests) do not already exist.
         - **Entity Existence & Status**: Verifying target records exist in database and are not soft-deleted.
         - **Calculations & Domain Invariants**: Cross-field business rules, mathematical calculations, quota checks, state machine transitions.
         - **Resource Ownership Rules**: Verifying that a non-admin actor owns the specific database record being modified/deleted.
       - Base `UseCase[Rq, Rp]` provides the `validate(request)` hook. Every concrete UseCase **MUST extend and implement `validate(request)`** for domain validations.
       - Services act as orchestrators delegating workflow execution to UseCases and must NOT contain inline validation logic.

4. **`app/api/` (System & Client Portals)**
   - **Contains**:
     - `app/api/system/`: Back-office / admin portal route handlers (`/api/system`).
     - `app/api/client/`: Store / merchant / customer portal route handlers (`/api/client`).
   - **Strict Rule**:
     - Allowed dependencies: **`app/service/` and `app/common/`**.
     - **NEVER** depend directly on `app/repository/` or bypass the service layer to execute database queries.
     - Controllers must be **lean**: parse/validate inputs, delegate to service, and return `RestResponse.ok(result)`.
     - **NO `summary` or `description` in route decorators**: Do NOT define `summary` or `description` attributes on controller route decorators (e.g. `@router.get`, `@router.post`) or parameters. They are unnecessary; API documentation is maintained in `FRONTEND_API.md`.

---

## 2. Standard Implementation Order for Any New Feature

Whenever creating a new domain entity or feature module (e.g. `products`, `orders`, `stores`), you **MUST** follow this exact step-by-step sequence:

```text
Step 1: Define Model (app/common/models.py or app/modules/<feature>/models.py)
   │    └─ Must extend BaseModel (inherits UUID primary key & full audit trail)
   ▼
Step 2: Define Schemas & DTOs (app/modules/<feature>/schemas.py or DTO records)
   │    └─ Request DTOs (with @NotNull, @NotBlank, @Size, @Min, @Max validations), Response DTOs, and FeatureFilter extending AbstractFilter
   ▼
Step 3: Define Repository (app/modules/<feature>/repository.py)
   │    └─ Queries, pagination, filtering, persistence
   ▼
Step 4: Define UseCases & Service (app/modules/<feature>/usecases.py & service.py)
   │    ├─ Concrete UseCases extending UseCase[Rq, Rp] implementing domain validations in validate() hook:
   │    │  (Duplicate/uniqueness checks, existence, calculations, invariants, authorization ONLY — DO NOT re-check null/blank/min/max)
   │    └─ Service class delegating operations to UseCases (zero inline validations in service)
   ▼
Step 5: Define API Routes (app/api/system/<feature>.py or app/api/client/<feature>.py)
   │    └─ Lean route handlers returning RestResponse.ok(...) (omit summary/description in decorators)
   ▼
Step 6: Register Route in Central Router (app/api/router.py)
   │    └─ Mount under /api/system or /api/client
   ▼
Step 7: Update FRONTEND_API.md (MANDATORY in the same commit/change)
   │    └─ Document paths, methods, permissions, schemas, sample JSON
   ▼
Step 8: Ready for User Verification
        └─ Code changes complete; user executes tests manually
```

---

## 3. Data Model Standards (`BaseModel` & Audit Fields)

Every database model representing a table **MUST** extend [`BaseModel`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/common/models.py#L110-L162). This automatically guarantees the entity carries:

| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | `UUID` | Primary key (`default=uuid.uuid4`) |
| `created_on` | `DateTime(UTC)` | Record creation timestamp |
| `created_by` | `String(36)` | User identifier who created the record (nullable) |
| `last_modified_on` | `DateTime(UTC)` | Auto-updating timestamp of last modification |
| `last_modified_by` | `String(36)` | User identifier who last modified the record (nullable) |
| `deleted_on` | `DateTime(UTC)` | Soft-delete timestamp (nullable) |
| `deleted_by` | `String(36)` | User identifier who soft-deleted the record (nullable) |
| `active` | `Boolean` | Flag indicating if record is active (default `True`) |
| `deleted` | `Boolean` | Flag indicating if record is soft-deleted (default `False`) |
| `remark` | `Text` | Optional audit remarks / operation notes |
| `version` | `BigInteger` | Optimistic locking counter (default `1`) |

### Model Utility Methods
- `entity.soft_delete(deleted_by=...)`: Marks `deleted=True`, `active=False`, stamps `deleted_on`.
- `entity.restore()`: Resets `deleted=False`, `active=True`, clears delete stamps.
- `entity.to_dict()`: Exports all table columns to a Python dictionary.

---

## 4. API Response Envelope & Error Handling Rules

### Standard Success Envelope (`ResponseTemplate<T>`)
All endpoint handlers return [`ResponseTemplate[T]`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/common/schemas.py#L20-L26) via [`RestResponse`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/common/schemas.py#L48-L80):

```json
{
  "code": "0",
  "message": "Success",
  "data": { ... },
  "time": "2026-10-22T09:30:00.123456"
}
```

* `code`: String `"0"` **always** indicates success.
* `message`: Human-readable summary string (defaults to `"Success"`).
* `data`: Payload object, list, or scalar.
* `time`: Server ISO 8601 timestamp generated by [`DateUtils.now()`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/common/utils.py#L8-L10).

### Standard Error Handling
Controllers and services **must never** return raw error dictionaries or inconsistent error envelopes. Raise custom exceptions from [`app.core.exceptions`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/core/exceptions.py):

* [`AppException("Error message")`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/core/exceptions.py#L48-L100): General business logic or validation error (HTTP 400).
* [`NotFoundException("Entity not found: {id}")`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/core/exceptions.py#L125-L145): Missing resource (HTTP 404).
* [`ForbiddenException("Access denied")`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/core/exceptions.py#L147-L163): Insufficient permissions (HTTP 403).
* [`UnauthorizedException("Authentication required")`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/core/exceptions.py#L164-L185): Invalid/missing credentials (HTTP 401).

Global exception handlers format all errors into the standard envelope:
```json
{
  "code": "400",
  "message": "Validation or business error message",
  "data": null,
  "time": "2026-10-22T09:30:00.123456"
}
```

---

## 5. Pagination & Search Criteria Rules

All paginated endpoints must follow these conventions:
1. Accept an [`AbstractFilter`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/common/pagination.py#L22-L52) (or a subclass) in the request body (typically `POST /search`).
2. Return [`ResponseTemplate[Page[T]]`](file:///Users/nishanranamagar/Documents/projects/danphe/rivo/backend/app/common/pagination.py#L13-L19) containing:
   * `totalRecords`: integer count of matched records.
   * `limit`: records per page (`pageSize`).
   * `currentPage`: page index (`pageNumber`, 1-indexed).
   * `data`: list of records.

---

## 6. Mandatory Frontend API Documentation Rule

> [!IMPORTANT]
> Whenever any new CRUD endpoint, parameter, request field, response field, or authorization requirement is created, modified, or deleted, you **MUST immediately update [`FRONTEND_API.md`](FRONTEND_API.md)** in the same commit/changeset.

The documentation must always include:
- HTTP method and exact URL path.
- Security & authorization tier (`PUBLIC`, `AUTHENTICATED`, `ROLE_RESTRICTED`).
- Request parameters and body schema with field types.
- Response envelope and sample JSON payload.

---

## 7. Verification Workflow

Before concluding any backend coding task:
1. **Never run tests**: The agent must **NEVER** run tests (`unittest`, `pytest`, etc.) automatically. All testing is managed and executed manually by the user.
2. **Verify that [`FRONTEND_API.md`](FRONTEND_API.md) is updated** and accurately reflects all changes made.

---

## 8. Agent Permissions & Operational Boundaries

1. **Full Autonomy Inside the Project (`rivo`)**:
   - The agent is pre-authorized to read, create, and modify files, execute commands, and perform builds inside this repository (`/Users/nishanranamagar/Documents/projects/danphe/rivo`) without asking for confirmation.
   - When the user requests changes, implement them immediately. Do not pause to ask for trivial confirmations or routine permissions for actions inside this project.
2. **Mandatory Permission for Deletions**:
   - The agent must **ALWAYS** ask for explicit user permission before deleting any file or directory (e.g. `rm`, `rm -rf`, `os.remove`, or file deletion tools) inside or outside the repository.
3. **External File Boundary (Outside `rivo`)**:
   - The agent must **NEVER** inspect, read, view, create, edit, or manipulate any files or directories located **outside** this repository without asking for explicit user permission first.
4. **Code Cleanliness & Comments**:
   - Maintain zero `#` comments and zero docstrings in application code (`backend/app/`). Code must remain clean, self-documenting, and free of extraneous comments.
5. **Testing Data Limit (Token & Resource Optimization)**:
   - When writing test code across all CRUD operations, test with at most 5 data items / sample records (never more than 5).
   - If 5 records are not sufficient to verify a scenario, pause and ask the user how many more test records to use.
6. **Strict Prohibition on Running Tests**:
   - The agent must **NEVER** run tests on its own under any circumstances. All tests are turned off for the agent; the user will test everything manually.
7. **Strict Prohibition on Redundant Auth & Null/Blank/Min/Max in UseCases**:
   - Authentication and route authorization are enforced globally by Spring Security (`SecurityConfig` / `@PreAuthorize`). Never re-check if the actor is authenticated (`actor == null`, `actor.getId() == null`) in UseCase `validate()`.
   - Never re-check null, blank, string lengths, or min/max values in UseCase `validate()` when they are already validated by the Request DTO record.
   - UseCase `validate()` must strictly be reserved for duplicate value checks against repositories, database lookups, business logic calculations, state transitions, and resource ownership checks.

