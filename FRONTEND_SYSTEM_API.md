# System Portal (Back-Office / Admin) Frontend API Contract & Integration Guide

This document is the single source of truth for Back-Office, Admin, and Operations frontend applications communicating with the **System Portal API** service on port **8021** (`/api/system`).

> [!IMPORTANT]
> **RULE BOOK REQUIREMENT**: Every time a system endpoint, request parameter, or response field is added or modified, this file **MUST** be updated immediately in the same commit/change.

---

## 1. General Architecture & Envelopes

### Base URLs & Service Discovery
- **System Portal API Base URL**: `http://localhost:8021/api/system`
- **Interactive Swagger Documentation**: [`http://localhost:8021/swagger-ui.html`](http://localhost:8021/swagger-ui.html)
- **OpenAPI Schema (JSON)**: [`http://localhost:8021/v3/api-docs`](http://localhost:8021/v3/api-docs)
- **Frontend Vite Dev Proxy**: `http://localhost:5173` (proxies `/api/system` → `http://localhost:8021`)

### Global Response Envelope (`ResponseTemplate<T>`)
Every JSON response from the System Portal backend is wrapped in the standard `ResponseTemplate<T>` matching `bazar-backend`:

```json
{
  "code": "0",
  "message": "Success",
  "data": { ... },
  "time": "2026-10-30T18:00:00.123456"
}
```

- `code`: Status code string (`"0"` indicates success, HTTP error codes e.g. `"400"`, `"401"`, `"403"`, `"404"`, `"500"` for failures).
- `message`: User-friendly status message.
- `data`: Payload containing the requested entity, paginated list, or scalar value.
- `time`: Server ISO-8601 timestamp string.

### Global Error Format
When an exception occurs (e.g. 400, 401, 403, 404, 500), the response body is formatted consistently:

```json
{
  "code": "400",
  "message": "Validation or business error message",
  "data": null,
  "time": "2026-10-30T18:00:00.123456"
}
```

### Standard Pagination Envelope (`Page<T>`)
Endpoints ending in `/search` return `Page<T>` inside the envelope `data` property:

```json
{
  "code": "0",
  "message": "Success",
  "data": {
    "totalRecords": 45,
    "limit": 10,
    "currentPage": 1,
    "data": [
      { /* Item 1 */ },
      { /* Item 2 */ }
    ]
  },
  "time": "2026-10-30T18:00:00.123456"
}
```

### Standard Filter Body (`AbstractFilter`)
All search and filter endpoints accept a JSON filter body extending `AbstractFilter`:

```json
{
  "pageNumber": 1,
  "pageSize": 10,
  "searchParameter": "keyword to search",
  "sortField": "created_on",
  "sortOrder": "DESC",
  "active": true
}
```

### Authentication & Administrative Security Standards
- **Administrative Roles**: Access requires administrative privileges (`SYSTEM_USER`, `ADMIN`, or `STAFF`). Non-system users attempting to access system routes will be rejected with `403 Forbidden`.
- **Authentication Channels**:
  - **HttpOnly Cookies**: `access_token` and `refresh_token`. Automatically set upon successful login and included by browser requests.
  - **Bearer Header**: `Authorization: Bearer <token>` for programmatic API or script clients.
- **CORS & Credentials**:
  - Frontend fetch / axios requests **MUST** include credentials (`credentials: 'include'` in `fetch` or `withCredentials: true` in `axios`).
  - Allowed origins in development: `http://localhost:3000`, `http://localhost:5173`, `http://localhost:8000`, `http://localhost:8021`, `http://localhost:8022`.

---

## 2. Health & Diagnostics

### 2.1 Health Check (`GET /health`)
- **Authority / Permission**: None (🟢 **PUBLIC**)
- **Method**: `GET`
- **Path**: `/health`
- **Port**: 8021
- **Response**: `200 OK`
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": {
      "status": "healthy",
      "environment": "development",
      "database": "connected"
    },
    "time": "2026-10-30T18:00:00.123456"
  }
  ```

### 2.2 System Ping (`GET /api/system/ping`)
- **Authority / Permission**: None (🟢 **PUBLIC**)
- **Method**: `GET`
- **Path**: `/api/system/ping`
- **Response**: `200 OK`
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": "pong",
    "time": "2026-10-30T18:00:00.123456"
  }
  ```

---

## 3. Administrative Authentication (`/api/system/auth`)

### 3.1 System Login (`POST /api/system/auth/login`)
- **Authority / Permission**: None (🟢 **PUBLIC**)
- **Method**: `POST`
- **Path**: `/api/system/auth/login`
- **Access Rule**: Requires administrative role (`SYSTEM_USER`, `ADMIN`, or `STAFF`). Non-system users are rejected with HTTP 403.
- **Default Seeded Credentials (matching Bazar)**:
  - `email`: `superadmin@bazar.com` (or `superadmin@rivo.com`)
  - `password`: `Admin@1234`
- **Request Body (`LoginRequest`)**:
  ```json
  {
    "email": "superadmin@bazar.com",
    "password": "Admin@1234"
  }
  ```
- **Cookies Set**:
  - `access_token`: HttpOnly; Path=/; Max-Age=86400 (24 hours); SameSite=Lax
  - `refresh_token`: HttpOnly; Path=/; Max-Age=604800 (7 days); SameSite=Lax
- **Errors**:
  - `401 Unauthorized`: Invalid email or password (`{"code": "401", "message": "Invalid email or password"}`) or inactive account (`{"code": "401", "message": "User account is inactive"}`).
  - `403 Forbidden`: Non-system users (`{"code": "403", "message": "Access denied: Administrative privileges required"}`).
  - `429 Too Many Requests`: Exceeded 5 failed attempts within 1 minute.
- **Response**: `200 OK`
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": {
      "id": "e8334460-aefb-4171-872f-5ea3b1187c32",
      "name": "Admin User",
      "email": "admin@example.com",
      "phone": "+1234567890",
      "role": "SYSTEM_USER",
      "roles": [
        {
          "id": "f1234567-89ab-cdef-0123-456789abcdef",
          "user_id": "e8334460-aefb-4171-872f-5ea3b1187c32",
          "role": "SYSTEM_USER",
          "created_on": "2026-10-26T14:50:00Z",
          "modified_on": "2026-10-26T14:50:00Z",
          "active": true,
          "deleted": false
        }
      ],
      "access_token": "<jwt-token>",
      "refresh_token": "<jwt-token>",
      "token_type": "Bearer",
      "created_on": "2026-10-26T14:50:00Z",
      "modified_on": "2026-10-26T14:50:00Z",
      "active": true,
      "deleted": false
    },
    "time": "2026-10-30T18:00:00.123456"
  }
  ```

### 3.2 System Token Refresh (`POST /api/system/auth/refresh`)
- **Authority / Permission**: Authenticated via `refresh_token` cookie or request body
- **Method**: `POST`
- **Path**: `/api/system/auth/refresh`
- **Request Body (Optional if cookie present)**:
  ```json
  {
    "refresh_token": "<jwt-refresh-token>"
  }
  ```
- **Cookies Updated**: `access_token`, `refresh_token`
- **Response**: `200 OK`
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": {
      "access_token": "<new-jwt-access-token>",
      "refresh_token": "<new-jwt-refresh-token>",
      "token_type": "Bearer"
    },
    "time": "2026-10-30T18:00:00.123456"
  }
  ```

### 3.3 System Logout (`POST /api/system/auth/logout`)
- **Authority / Permission**: None (🟢 **PUBLIC**)
- **Method**: `POST`
- **Path**: `/api/system/auth/logout`
- **Cookies Cleared**: `access_token`, `refresh_token`
- **Response**: `200 OK`
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": null,
    "time": "2026-10-30T18:00:00.123456"
  }
  ```

### 3.4 System Current User Profile (`GET /api/system/auth/me`)
- **Authority / Permission**: Authenticated Admin/Staff (🔒 **ROLE_RESTRICTED**: `SYSTEM_USER`, `ADMIN`, `STAFF`)
- **Method**: `GET`
- **Path**: `/api/system/auth/me`
- **Response**: `200 OK` (`UserResponse`)

---

## 4. User Administration (`/api/system/users`)

Administrative management of system, staff, merchant, and client accounts.

### 4.1 Create User (`POST /api/system/users`)
- **Authority / Permission**: Authenticated Admin (🔒 **ROLE_RESTRICTED**: `SYSTEM_USER`, `ADMIN`)
- **Method**: `POST`
- **Path**: `/api/system/users`
- **Request Body (`UserCreateDto`)**:
  ```json
  {
    "name": "Jane Doe",
    "email": "jane.doe@example.com",
    "password": "AdminSetPassword123!",
    "phone": "+1234567890",
    "role": "USER"
  }
  ```
- **Response**: `200 OK` (`UserResponse`)

### 4.2 Get User by ID (`GET /api/system/users/{user_id}`)
- **Authority / Permission**: Authenticated Admin (🔒 **ROLE_RESTRICTED**: `SYSTEM_USER`, `ADMIN`, `STAFF`)
- **Method**: `GET`
- **Path**: `/api/system/users/{user_id}`
- **Path Parameters**: `user_id` (UUID string)
- **Response**: `200 OK` (`UserResponse`)

### 4.3 Update User (`PUT /api/system/users/{user_id}`)
- **Authority / Permission**: Authenticated Admin (🔒 **ROLE_RESTRICTED**: `SYSTEM_USER`, `ADMIN`)
- **Method**: `PUT`
- **Path**: `/api/system/users/{user_id}`
- **Path Parameters**: `user_id` (UUID string)
- **Request Body (`UserUpdateDto`)**:
  ```json
  {
    "name": "Jane Doe Updated",
    "email": "jane.updated@example.com",
    "phone": "+9876543210",
    "role": "ADMIN",
    "active": true
  }
  ```
- **Response**: `200 OK` (`UserResponse`)

### 4.4 Delete User (`DELETE /api/system/users/{user_id}`)
- **Authority / Permission**: Authenticated Admin (🔒 **ROLE_RESTRICTED**: `SYSTEM_USER`, `ADMIN`)
- **Method**: `DELETE`
- **Path**: `/api/system/users/{user_id}`
- **Path Parameters**: `user_id` (UUID string)
- **Response**: `200 OK`
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": null,
    "time": "2026-10-30T18:00:00.123456"
  }
  ```

### 4.5 Search Users (`POST /api/system/users/search`)
- **Authority / Permission**: Authenticated Admin (🔒 **ROLE_RESTRICTED**: `SYSTEM_USER`, `ADMIN`, `STAFF`)
- **Method**: `POST`
- **Path**: `/api/system/users/search`
- **Request Body (`UserFilter`)**:
  ```json
  {
    "pageNumber": 1,
    "pageSize": 10,
    "searchParameter": "Jane",
    "role": "USER",
    "sortField": "created_on",
    "sortOrder": "DESC",
    "active": true
  }
  ```
- **Response**: `200 OK` (`Page<UserResponse>`)

### 4.6 List Users (`POST /api/system/users/list` or `GET /api/system/users`)
- **Authority / Permission**: Authenticated Admin (🔒 **ROLE_RESTRICTED**: `SYSTEM_USER`, `ADMIN`, `STAFF`)
- **Method**: `POST` / `GET`
- **Path**: `/api/system/users/list` or `/api/system/users`
- **Response**: `200 OK` (`Page<UserResponse>`)

---

## 5. Master Data Management (`/api/system/master-data`)

Configuration categories for application lookups (e.g. `GAME`, `CATEGORY`, `POSITION`).

### 5.1 Create Master Data (`POST /api/system/master-data`)
- **Authority / Permission**: Administrative / System User (🔒 **ROLE_RESTRICTED**: `SYSTEM_USER`, `ADMIN`, `STAFF`)
- **Method**: `POST`
- **Path**: `/api/system/master-data`
- **Description**: Creates a new master data entry for system lookups. The `code` field represents the grouping category (e.g. `GAME`, `CATEGORY`, `POSITION`) and can be repeated across multiple entries. The combination of `(code, title)` is unique among non-deleted entries.
- **Request Body (`MasterDataCreateDto`)**:
  ```json
  {
    "title": "Futsal",
    "code": "GAME"
  }
  ```
- **Fields & Validations (`MasterDataCreateDto`)**:
  - `title`: string, **required**, `@NotBlank`, `@Size(max = 255)`
  - `code`: string, **required**, `@NotBlank`, `@Size(max = 100)`, represents category/grouping code (automatically normalized to uppercase).
- **Response**: `200 OK`
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": {
      "id": "c1f7a61d-8692-426b-80a9-19601d3cb863",
      "title": "Futsal",
      "code": "GAME",
      "created_on": "2026-10-30T16:15:00",
      "modified_on": "2026-10-30T16:15:00",
      "created_by": "e8334460-aefb-4171-872f-5ea3b1187c32",
      "modified_by": "e8334460-aefb-4171-872f-5ea3b1187c32",
      "active": true,
      "deleted": false
    },
    "time": "2026-10-30T16:15:00.123456"
  }
  ```

### 5.2 Get Master Data by ID (`GET /api/system/master-data/{id}`)
- **Authority / Permission**: Administrative / System User (🔒 **ROLE_RESTRICTED**: `SYSTEM_USER`, `ADMIN`, `STAFF`)
- **Method**: `GET`
- **Path**: `/api/system/master-data/{id}`
- **Path Parameters**: `id` (UUID string)
- **Response**: `200 OK` (`MasterDataResponseDto`)

### 5.3 Get Master Data by Code (`GET /api/system/master-data/code/{code}`)
- **Authority / Permission**: Administrative / System User (🔒 **ROLE_RESTRICTED**: `SYSTEM_USER`, `ADMIN`, `STAFF`)
- **Method**: `GET`
- **Path**: `/api/system/master-data/code/{code}`
- **Path Parameters**: `code` (string, e.g. `GAME`, `CATEGORY`)
- **Description**: Returns all non-deleted master data items under the specified code category ordered by title.
- **Response**: `200 OK` (`List<MasterDataResponseDto>`)
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": [
      {
        "id": "c1f7a61d-8692-426b-80a9-19601d3cb863",
        "title": "Basketball",
        "code": "GAME",
        "created_on": "2026-10-30T16:15:00",
        "modified_on": "2026-10-30T16:15:00",
        "active": true,
        "deleted": false
      },
      {
        "id": "d2e8b72e-9703-537c-91ba-20712e4dc974",
        "title": "Futsal",
        "code": "GAME",
        "created_on": "2026-10-30T16:16:00",
        "modified_on": "2026-10-30T16:16:00",
        "active": true,
        "deleted": false
      }
    ],
    "time": "2026-10-30T16:16:00.123456"
  }
  ```

### 5.4 Update Master Data (`PUT /api/system/master-data/{id}`)
- **Authority / Permission**: Administrative / System User (🔒 **ROLE_RESTRICTED**: `SYSTEM_USER`, `ADMIN`, `STAFF`)
- **Method**: `PUT`
- **Path**: `/api/system/master-data/{id}`
- **Path Parameters**: `id` (UUID string)
- **Request Body (`MasterDataUpdateDto`)**:
  ```json
  {
    "title": "5v5 Futsal",
    "code": "GAME"
  }
  ```
- **Fields & Validations (`MasterDataUpdateDto`)**:
  - `title`: string, optional, `@Size(max = 255)`
  - `code`: string, optional, `@Size(max = 100)`
- **Response**: `200 OK` (`MasterDataResponseDto`)

### 5.5 Delete Master Data (`DELETE /api/system/master-data/{id}`)
- **Authority / Permission**: Administrative / System User (🔒 **ROLE_RESTRICTED**: `SYSTEM_USER`, `ADMIN`, `STAFF`)
- **Method**: `DELETE`
- **Path**: `/api/system/master-data/{id}`
- **Path Parameters**: `id` (UUID string)
- **Response**: `200 OK`
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": null,
    "time": "2026-10-30T16:15:00.123456"
  }
  ```

### 5.6 Search Master Data (`POST /api/system/master-data/search`)
- **Authority / Permission**: Administrative / System User (🔒 **ROLE_RESTRICTED**: `SYSTEM_USER`, `ADMIN`, `STAFF`)
- **Method**: `POST`
- **Path**: `/api/system/master-data/search`
- **Request Body (`MasterDataFilter`)**:
  ```json
  {
    "pageNumber": 1,
    "pageSize": 10,
    "searchParameter": "Futsal",
    "title": "Futsal",
    "code": "GAME",
    "sortField": "created_on",
    "sortOrder": "DESC",
    "active": true
  }
  ```
- **Response**: `200 OK` (`Page<MasterDataResponseDto>`)

---

## 6. Master Data Items Management (`/api/system/master-data-items`)

Hierarchical child items belonging to a parent Master Data category (e.g. specific game modes, equipment types).

### 6.1 Create Master Data Item (`POST /api/system/master-data-items`)
- **Authority / Permission**: Administrative / System User (🔒 **ROLE_RESTRICTED**: `SYSTEM_USER`, `ADMIN`, `STAFF`)
- **Method**: `POST`
- **Path**: `/api/system/master-data-items`
- **Request Body (`MasterDataItemCreateDto`)**:
  ```json
  {
    "master_data_id": "c1f7a61d-8692-426b-80a9-19601d3cb863",
    "title": "PUBG Mobile",
    "code": "PUBG",
    "description": "Battle Royale game category item"
  }
  ```
- **Fields & Validations (`MasterDataItemCreateDto`)**:
  - `master_data_id`: string (UUID), **required**, `@NotBlank`
  - `title`: string, **required**, `@NotBlank`, `@Size(max = 255)`
  - `code`: string, optional, `@Size(max = 100)`
  - `description`: string, optional
- **Response**: `200 OK`
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": {
      "id": "d2f8b72e-9703-537c-91ba-20712e4dc974",
      "master_data_id": "c1f7a61d-8692-426b-80a9-19601d3cb863",
      "title": "PUBG Mobile",
      "code": "PUBG",
      "description": "Battle Royale game category item",
      "created_on": "2026-10-30T20:25:00",
      "modified_on": "2026-10-30T20:25:00",
      "created_by": "e8334460-aefb-4171-872f-5ea3b1187c32",
      "modified_by": "e8334460-aefb-4171-872f-5ea3b1187c32",
      "active": true,
      "deleted": false
    },
    "time": "2026-10-30T20:25:00.123456"
  }
  ```

### 6.2 Get Master Data Item by ID (`GET /api/system/master-data-items/{id}`)
- **Authority / Permission**: Administrative / System User (🔒 **ROLE_RESTRICTED**: `SYSTEM_USER`, `ADMIN`, `STAFF`)
- **Method**: `GET`
- **Path**: `/api/system/master-data-items/{id}`
- **Path Parameters**: `id` (UUID string)
- **Response**: `200 OK` (`MasterDataItemResponseDto`)

### 6.3 Get Master Data Items by Parent Master Data ID (`GET /api/system/master-data-items/master-data/{masterDataId}`)
- **Authority / Permission**: Administrative / System User (🔒 **ROLE_RESTRICTED**: `SYSTEM_USER`, `ADMIN`, `STAFF`)
- **Method**: `GET`
- **Path**: `/api/system/master-data-items/master-data/{masterDataId}`
- **Path Parameters**: `masterDataId` (UUID string)
- **Response**: `200 OK` (`List<MasterDataItemResponseDto>`)

### 6.4 Update Master Data Item (`PUT /api/system/master-data-items/{id}`)
- **Authority / Permission**: Administrative / System User (🔒 **ROLE_RESTRICTED**: `SYSTEM_USER`, `ADMIN`, `STAFF`)
- **Method**: `PUT`
- **Path**: `/api/system/master-data-items/{id}`
- **Path Parameters**: `id` (UUID string)
- **Request Body (`MasterDataItemUpdateDto`)**:
  ```json
  {
    "master_data_id": "c1f7a61d-8692-426b-80a9-19601d3cb863",
    "title": "eFootball 2026",
    "code": "EFOOTBALL",
    "description": "Updated sports simulation game"
  }
  ```
- **Response**: `200 OK` (`MasterDataItemResponseDto`)

### 6.5 Delete Master Data Item (`DELETE /api/system/master-data-items/{id}`)
- **Authority / Permission**: Administrative / System User (🔒 **ROLE_RESTRICTED**: `SYSTEM_USER`, `ADMIN`, `STAFF`)
- **Method**: `DELETE`
- **Path**: `/api/system/master-data-items/{id}`
- **Path Parameters**: `id` (UUID string)
- **Response**: `200 OK`
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": null,
    "time": "2026-10-30T20:25:00.123456"
  }
  ```

### 6.6 Search Master Data Items (`POST /api/system/master-data-items/search`)
- **Authority / Permission**: Administrative / System User (🔒 **ROLE_RESTRICTED**: `SYSTEM_USER`, `ADMIN`, `STAFF`)
- **Method**: `POST`
- **Path**: `/api/system/master-data-items/search`
- **Request Body (`MasterDataItemFilter`)**:
  ```json
  {
    "pageNumber": 1,
    "pageSize": 10,
    "masterDataId": "c1f7a61d-8692-426b-80a9-19601d3cb863",
    "title": "PUBG",
    "code": "PUBG",
    "sortField": "created_on",
    "sortOrder": "DESC",
    "active": true
  }
  ```
- **Response**: `200 OK` (`Page<MasterDataItemResponseDto>`)

---

## 7. Match Requests Administration (`/api/system/match-requests`)

Full administrative oversight, moderation, creation, update, deletion, and searching of all match requests across the entire system.

### 7.1 Create Match Request (`POST /api/system/match-requests`)
- **Authority / Permission**: Administrative / System User (🔒 **ROLE_RESTRICTED**: `SYSTEM_USER`, `ADMIN`, `STAFF`)
- **Method**: `POST`
- **Path**: `/api/system/match-requests`
- **Request Body (`MatchRequestCreateDto`)**:
  ```json
  {
    "game": "Futsal",
    "request_type": "OPPONENT",
    "category": "5v5 Tournament",
    "message": "Official administrative tournament match request",
    "time": "2026-10-30T19:00:00",
    "urgent": true
  }
  ```
- **Fields & Validations (`MatchRequestCreateDto`)**:
  - `game`: string, **required**, `@NotBlank`, `@Size(max = 100)`
  - `request_type`: string, **required**, `@NotNull`, `@Pattern("OPPONENT|TEAMMATE")`
  - `category`: string, optional, `@Size(max = 100)`
  - `message`: string, optional, `@Size(max = 2000)`
  - `time`: string (ISO-8601 date-time), optional
  - `urgent`: boolean, optional
- **Response**: `200 OK` (`MatchRequestResponseDto`)
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": {
      "id": "e8334460-aefb-4171-872f-5ea3b1187c32",
      "requested_by": "e8334460-aefb-4171-872f-5ea3b1187c32",
      "request_type": "OPPONENT",
      "status": "OPEN",
      "game": "Futsal",
      "category": "5v5 Tournament",
      "message": "Official administrative tournament match request",
      "time": "2026-10-30T19:00:00",
      "urgent": true,
      "created_on": "2026-10-30T14:40:00",
      "modified_on": "2026-10-30T14:40:00",
      "created_by": "e8334460-aefb-4171-872f-5ea3b1187c32",
      "modified_by": "e8334460-aefb-4171-872f-5ea3b1187c32",
      "active": true,
      "deleted": false
    },
    "time": "2026-10-30T14:40:00.123456"
  }
  ```

### 7.2 Get Match Request by ID (`GET /api/system/match-requests/{id}`)
- **Authority / Permission**: Administrative / System User (🔒 **ROLE_RESTRICTED**: `SYSTEM_USER`, `ADMIN`, `STAFF`)
- **Method**: `GET`
- **Path**: `/api/system/match-requests/{id}`
- **Path Parameters**: `id` (UUID string)
- **Response**: `200 OK` (`MatchRequestResponseDto`)

### 7.3 Update Match Request by ID (`PUT /api/system/match-requests/{id}`)
- **Authority / Permission**: Administrative / System User (🔒 **ROLE_RESTRICTED**: `SYSTEM_USER`, `ADMIN`, `STAFF`)
- **Method**: `PUT`
- **Path**: `/api/system/match-requests/{id}`
- **Path Parameters**: `id` (UUID string)
- **Request Body (`MatchRequestUpdateDto`)**:
  ```json
  {
    "game": "Futsal",
    "request_type": "OPPONENT",
    "status": "MATCHED",
    "category": "5v5 Tournament",
    "message": "Updated match notes and status",
    "time": "2026-10-30T20:00:00",
    "urgent": false
  }
  ```
- **Fields & Validations (`MatchRequestUpdateDto`)**:
  - `game`: string, optional, `@Size(max = 100)`
  - `request_type`: string, optional, `@Pattern("OPPONENT|TEAMMATE")`
  - `status`: string, optional, `@Pattern("OPEN|MATCHED|FILLED|CANCELLED|COMPLETED")`
  - `category`: string, optional, `@Size(max = 100)`
  - `message`: string, optional, `@Size(max = 2000)`
  - `time`: string (ISO-8601 date-time), optional
  - `urgent`: boolean, optional
- **Response**: `200 OK` (`MatchRequestResponseDto`)

### 7.4 Delete Match Request by ID (`DELETE /api/system/match-requests/{id}`)
- **Authority / Permission**: Administrative / System User (🔒 **ROLE_RESTRICTED**: `SYSTEM_USER`, `ADMIN`, `STAFF`)
- **Method**: `DELETE`
- **Path**: `/api/system/match-requests/{id}`
- **Path Parameters**: `id` (UUID string)
- **Response**: `200 OK`
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": null,
    "time": "2026-10-30T14:45:00.123456"
  }
  ```

### 7.5 Search Match Requests (`POST /api/system/match-requests/search`)
- **Authority / Permission**: Administrative / System User (🔒 **ROLE_RESTRICTED**: `SYSTEM_USER`, `ADMIN`, `STAFF`)
- **Method**: `POST`
- **Path**: `/api/system/match-requests/search`
- **Request Body (`MatchRequestFilter`)**:
  ```json
  {
    "pageNumber": 1,
    "pageSize": 10,
    "sortField": "created_on",
    "sortOrder": "DESC",
    "game": "Futsal",
    "requestType": "OPPONENT",
    "status": "OPEN",
    "urgent": true,
    "requestedBy": "optional-user-id"
  }
  ```
- **Response**: `200 OK` (`Page<MatchRequestResponseDto>`)
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": {
      "totalRecords": 1,
      "limit": 10,
      "currentPage": 1,
      "data": [
        {
          "id": "e8334460-aefb-4171-872f-5ea3b1187c32",
          "requested_by": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
          "request_type": "OPPONENT",
          "status": "OPEN",
          "game": "Futsal",
          "category": "5v5 Tournament",
          "message": "Tournament match request",
          "time": "2026-10-30T19:00:00",
          "urgent": true,
          "created_on": "2026-10-30T14:40:00",
          "modified_on": "2026-10-30T14:40:00",
          "active": true,
          "deleted": false
        }
      ]
    },
    "time": "2026-10-30T14:45:00.123456"
  }
  ```
