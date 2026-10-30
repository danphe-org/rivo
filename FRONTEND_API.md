# Frontend API Contract & Integration Guide

This document is the single source of truth for Frontend (FE) web applications communicating with the backend services. It specifies the base envelopes, authentication flow, error formatting, and exact Request/Response payloads for all API endpoints, mirroring the standards of the `danphe/bazar_backend` project.

> [!IMPORTANT]
> **RULE BOOK REQUIREMENT**: Every time a new CRUD operation, endpoint, request parameter, or response field is added or modified, this file **MUST** be updated immediately in the same commit/change.

---

## 1. General Architecture & Envelopes

### Base URLs & Portals
- **System Portal API (Back-office / Admin)**: `http://localhost:8021/api/system`
- **Client Portal API (Store / Merchant / Customer)**: `http://localhost:8022/api/client`
- **System Interactive Swagger Documentation**: [`http://localhost:8021/swagger-ui.html`](http://localhost:8021/swagger-ui.html)
- **Client Interactive Swagger Documentation**: [`http://localhost:8022/swagger-ui.html`](http://localhost:8022/swagger-ui.html)
- **System OpenAPI Schema**: [`http://localhost:8021/v3/api-docs`](http://localhost:8021/v3/api-docs)
- **Client OpenAPI Schema**: [`http://localhost:8022/v3/api-docs`](http://localhost:8022/v3/api-docs)
- **Frontend Vite Dev Proxy**: `http://localhost:5173` (proxies `/api/system` → `http://localhost:8021` and `/api/client` → `http://localhost:8022`)

### Global Response Envelope (`ResponseTemplate<T>`)
Every JSON response from the backend is wrapped in the standard `ResponseTemplate<T>` matching `bazar-backend`:

```json
{
  "code": "0",
  "message": "Success",
  "data": { ... },
  "time": "2026-10-22T09:30:00.123456"
}
```

* `code`: Status code string (`"0"` indicates success, error/HTTP codes for failures).
* `message`: User-friendly status message.
* `data`: Payload containing the requested entity, paginated list, or scalar value.
* `time`: Server timestamp string.

### Global Error Format
When an exception occurs (e.g. 400, 401, 403, 404, 500), the response body is formatted consistently:

```json
{
  "code": "400",
  "message": "Validation or business error message",
  "data": null,
  "time": "2026-10-22T09:30:00.123456"
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
  "time": "2026-10-22T09:30:00.123456"
}
```

### Standard Filter Body (`AbstractFilter`)
All upcoming search and filter endpoints accept a JSON filter body extending `AbstractFilter`:

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

### Authentication & Security Standards
* **Authentication Channels**:
  * **HttpOnly Cookies**: `ACCESS_TOKEN` and `REFRESH_TOKEN`.
  * **Bearer Header**: `Authorization: Bearer <token>` for non-browser / mobile clients.
* **CORS & Credentials**:
  * Frontend fetch / axios requests **MUST** include credentials (`credentials: 'include'` in `fetch` or `withCredentials: true` in `axios`).
  * Allowed origins in development: `http://localhost:3000`, `http://localhost:5173`, `http://localhost:8000`, `http://localhost:8021`, `http://localhost:8022`.

---

## 2. System Portal APIs (`/api/system`)

Used exclusively by the back-office / admin portal on port 8021.

### 2.1 Health Check (`/api/system/ping`)
- **Authority / Permission**: None (🟢 **PUBLIC**)
- **Method**: `GET`
- **Path**: `/api/system/ping`
- **Response**: `200 OK`
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": "pong",
    "time": "2026-10-22T09:30:00.123456"
  }
  ```

### 2.2 System Login (`POST /api/system/auth/login`)
- **Authority / Permission**: None (🟢 **PUBLIC**)
- **Method**: `POST`
- **Path**: `/api/system/auth/login`
- **Access Rule**: Requires administrative role (`SYSTEM_USER`, `ADMIN`, or `STAFF`). Non-system users are rejected with HTTP 403.
- **Request Body (`LoginRequest`)**:
  ```json
  {
    "email": "admin@example.com",
    "password": "AdminPassword123!"
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
    "time": "2026-10-27T15:00:00.123456"
  }
  ```

### 2.3 System Token Refresh (`POST /api/system/auth/refresh`)
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
    "time": "2026-10-27T15:00:00.123456"
  }
  ```

### 2.4 System Logout (`POST /api/system/auth/logout`)
- **Authority / Permission**: None (🟢 **PUBLIC**)
- **Method**: `POST`
- **Path**: `/api/system/auth/logout`
- **Cookies Cleared**: `access_token`, `refresh_token`
- **Response**: `200 OK`

### 2.5 System Current User Profile (`GET /api/system/auth/me`)
- **Authority / Permission**: Authenticated (Admin/Staff)
- **Method**: `GET`
- **Path**: `/api/system/auth/me`
- **Response**: `200 OK` (`UserResponse`)

### 2.6 Create User (`POST /api/system/users`)
- **Authority / Permission**: Authenticated / Admin
- **Method**: `POST`
- **Path**: `/api/system/users`
- **Request Body**:
  ```json
  {
    "name": "Jane Doe",
    "email": "jane.doe@example.com",
    "password": "AdminSetPassword123!",
    "phone": "+1234567890",
    "role": "USER"
  }
  ```
- **Response**: `200 OK`

### 2.7 Get User by ID (`GET /api/system/users/{user_id}`)
- **Authority / Permission**: Authenticated / Admin
- **Method**: `GET`
- **Path**: `/api/system/users/{user_id}`
- **Path Parameters**: `user_id` (UUID string)
- **Response**: `200 OK`

### 2.8 Update User (`PUT /api/system/users/{user_id}`)
- **Authority / Permission**: Authenticated / Admin
- **Method**: `PUT`
- **Path**: `/api/system/users/{user_id}`
- **Path Parameters**: `user_id` (UUID string)
- **Request Body**:
  ```json
  {
    "name": "Jane Doe Updated",
    "email": "jane.updated@example.com",
    "phone": "+9876543210",
    "role": "ADMIN",
    "active": true
  }
  ```
- **Response**: `200 OK`

### 2.9 Delete User (`DELETE /api/system/users/{user_id}`)
- **Authority / Permission**: Authenticated / Admin
- **Method**: `DELETE`
- **Path**: `/api/system/users/{user_id}`
- **Path Parameters**: `user_id` (UUID string)
- **Response**: `200 OK`

### 2.10 Search Users (`POST /api/system/users/search`)
- **Authority / Permission**: Authenticated / Admin
- **Method**: `POST`
- **Path**: `/api/system/users/search`
- **Request Body**:
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

### 2.11 List Users (`POST /api/system/users/list` or `GET /api/system/users`)
- **Authority / Permission**: Authenticated / Admin
- **Method**: `POST` / `GET`
- **Path**: `/api/system/users/list` or `/api/system/users`
- **Response**: `200 OK` (`Page<UserResponse>`)

### 2.12 Create Master Data (`POST /api/system/master-data`)
- **Authority / Permission**: Administrative / System User (🔒 **ROLE_RESTRICTED**: `SYSTEM_USER`, `ADMIN`, `STAFF`)
- **Method**: `POST`
- **Path**: `/api/system/master-data`
- **Description**: Creates a new master data entry for system lookups (e.g. categories, games, types). The `code` field represents the grouping category (e.g. `GAME`, `CATEGORY`, `POSITION`) and can be repeated across multiple entries. The combination of `(code, title)` is unique among non-deleted entries.
- **Request Body (`MasterDataCreateDto`)**:
  ```json
  {
    "title": "Futsal",
    "code": "GAME"
  }
  ```
- **Fields & Validations (`MasterDataCreateDto`)**:
  - `title`: string, **required**, `@NotBlank`, `@Size(max = 255)`
  - `code`: string, **required**, `@NotBlank`, `@Size(max = 100)`, represents the category/grouping code (automatically normalized to uppercase). Multiple titles can belong to the same code category.
- **Response**: `200 OK`
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": {
      "id": "c1f7a61d-8692-426b-80a9-19601d3cb863",
      "title": "Futsal",
      "code": "GAME",
      "created_on": "2026-10-29T16:15:00",
      "modified_on": "2026-10-29T16:15:00",
      "created_by": "e8334460-aefb-4171-872f-5ea3b1187c32",
      "modified_by": "e8334460-aefb-4171-872f-5ea3b1187c32",
      "active": true,
      "deleted": false
    },
    "time": "2026-10-29T16:15:00.123456"
  }
  ```

### 2.13 Get Master Data by ID (`GET /api/system/master-data/{id}`)
- **Authority / Permission**: Administrative / System User (🔒 **ROLE_RESTRICTED**: `SYSTEM_USER`, `ADMIN`, `STAFF`)
- **Method**: `GET`
- **Path**: `/api/system/master-data/{id}`
- **Path Parameters**: `id` (UUID string)
- **Response**: `200 OK` (`MasterDataResponseDto`)
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": {
      "id": "c1f7a61d-8692-426b-80a9-19601d3cb863",
      "title": "Futsal",
      "code": "GAME",
      "created_on": "2026-10-29T16:15:00",
      "modified_on": "2026-10-29T16:15:00",
      "created_by": "e8334460-aefb-4171-872f-5ea3b1187c32",
      "modified_by": "e8334460-aefb-4171-872f-5ea3b1187c32",
      "active": true,
      "deleted": false
    },
    "time": "2026-10-29T16:15:00.123456"
  }
  ```

### 2.14 Get Master Data by Code (`GET /api/system/master-data/code/{code}`)
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
        "created_on": "2026-10-29T16:15:00",
        "modified_on": "2026-10-29T16:15:00",
        "active": true,
        "deleted": false
      },
      {
        "id": "d2e8b72e-9703-537c-91ba-20712e4dc974",
        "title": "Futsal",
        "code": "GAME",
        "created_on": "2026-10-29T16:16:00",
        "modified_on": "2026-10-29T16:16:00",
        "active": true,
        "deleted": false
      }
    ],
    "time": "2026-10-29T16:16:00.123456"
  }
  ```

### 2.15 Update Master Data (`PUT /api/system/master-data/{id}`)
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

### 2.16 Delete Master Data (`DELETE /api/system/master-data/{id}`)
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
    "time": "2026-10-29T16:15:00.123456"
  }
  ```

### 2.17 Search Master Data (`POST /api/system/master-data/search`)
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
          "id": "c1f7a61d-8692-426b-80a9-19601d3cb863",
          "title": "Futsal",
          "code": "FUTSAL",
          "created_on": "2026-10-29T16:15:00",
          "modified_on": "2026-10-29T16:15:00",
          "created_by": "e8334460-aefb-4171-872f-5ea3b1187c32",
          "modified_by": "e8334460-aefb-4171-872f-5ea3b1187c32",
          "active": true,
          "deleted": false
        }
      ]
    },
    "time": "2026-10-29T16:15:00.123456"
  }
  ```

### 2.18 Create Master Data Item (`POST /api/system/master-data-items`)
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
      "created_on": "2026-10-29T20:25:00",
      "modified_on": "2026-10-29T20:25:00",
      "created_by": "e8334460-aefb-4171-872f-5ea3b1187c32",
      "modified_by": "e8334460-aefb-4171-872f-5ea3b1187c32",
      "active": true,
      "deleted": false
    },
    "time": "2026-10-29T20:25:00.123456"
  }
  ```

### 2.19 Get Master Data Item by ID (`GET /api/system/master-data-items/{id}`)
- **Authority / Permission**: Administrative / System User (🔒 **ROLE_RESTRICTED**: `SYSTEM_USER`, `ADMIN`, `STAFF`)
- **Method**: `GET`
- **Path**: `/api/system/master-data-items/{id}`
- **Response**: `200 OK` (`MasterDataItemResponseDto`)

### 2.20 Get Master Data Items by Parent Master Data ID (`GET /api/system/master-data-items/master-data/{masterDataId}`)
- **Authority / Permission**: Administrative / System User (🔒 **ROLE_RESTRICTED**: `SYSTEM_USER`, `ADMIN`, `STAFF`)
- **Method**: `GET`
- **Path**: `/api/system/master-data-items/master-data/{masterDataId}`
- **Response**: `200 OK` (`List<MasterDataItemResponseDto>`)
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": [
      {
        "id": "d2f8b72e-9703-537c-91ba-20712e4dc974",
        "master_data_id": "c1f7a61d-8692-426b-80a9-19601d3cb863",
        "title": "PUBG Mobile",
        "code": "PUBG",
        "description": "Battle Royale game category item",
        "created_on": "2026-10-29T20:25:00",
        "modified_on": "2026-10-29T20:25:00",
        "created_by": "e8334460-aefb-4171-872f-5ea3b1187c32",
        "modified_by": "e8334460-aefb-4171-872f-5ea3b1187c32",
        "active": true,
        "deleted": false
      }
    ],
    "time": "2026-10-29T20:25:00.123456"
  }
  ```

### 2.21 Update Master Data Item (`PUT /api/system/master-data-items/{id}`)
- **Authority / Permission**: Administrative / System User (🔒 **ROLE_RESTRICTED**: `SYSTEM_USER`, `ADMIN`, `STAFF`)
- **Method**: `PUT`
- **Path**: `/api/system/master-data-items/{id}`
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

### 2.22 Delete Master Data Item (`DELETE /api/system/master-data-items/{id}`)
- **Authority / Permission**: Administrative / System User (🔒 **ROLE_RESTRICTED**: `SYSTEM_USER`, `ADMIN`, `STAFF`)
- **Method**: `DELETE`
- **Path**: `/api/system/master-data-items/{id}`
- **Response**: `200 OK`
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": null,
    "time": "2026-10-29T20:25:00.123456"
  }
  ```

### 2.23 Search Master Data Items (`POST /api/system/master-data-items/search`)
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
          "id": "d2f8b72e-9703-537c-91ba-20712e4dc974",
          "master_data_id": "c1f7a61d-8692-426b-80a9-19601d3cb863",
          "title": "PUBG Mobile",
          "code": "PUBG",
          "description": "Battle Royale game category item",
          "created_on": "2026-10-29T20:25:00",
          "modified_on": "2026-10-29T20:25:00",
          "created_by": "e8334460-aefb-4171-872f-5ea3b1187c32",
          "modified_by": "e8334460-aefb-4171-872f-5ea3b1187c32",
          "active": true,
          "deleted": false
        }
      ]
    },
    "time": "2026-10-29T20:25:00.123456"
  }
  ```

---

## 3. Client Portal APIs (`/api/client`)

Used by the client / store / customer portal on port 8022.

### 3.1 Health Check (`/api/client/ping`)
- **Authority / Permission**: None (🟢 **PUBLIC**)
- **Method**: `GET`
- **Path**: `/api/client/ping`
- **Response**: `200 OK`

### 3.2 Client Login (`POST /api/client/auth/login`)
- **Authority / Permission**: None (🟢 **PUBLIC**)
- **Method**: `POST`
- **Path**: `/api/client/auth/login`
- **Request Body**:
  ```json
  {
    "email": "customer@example.com",
    "password": "CustomerPassword123!"
  }
  ```
- **Cookies Set**: `access_token`, `refresh_token`
- **Errors**:
  - `401 Unauthorized`: Invalid email or password (`{"code": "401", "message": "Invalid email or password"}`) or inactive account (`{"code": "401", "message": "User account is inactive"}`).
  - `429 Too Many Requests`: Exceeded 5 failed attempts within 1 minute.
- **Response**: `200 OK` (`UserResponse` with tokens)

### 3.3 Client Register (`POST /api/client/auth/register`)
- **Authority / Permission**: None (🟢 **PUBLIC**)
- **Method**: `POST`
- **Path**: `/api/client/auth/register`
- **Request Body**:
  ```json
  {
    "name": "Jane Doe",
    "email": "jane.doe@example.com",
    "password": "SecurePassword123!",
    "phone": "+1234567890"
  }
  ```
- **Role Assignment**: Automatically assigned `role: "CLIENT"` by backend.
- **Cookies Set**: Automatically logs in user (`access_token`, `refresh_token` cookies set).
- **Response**: `200 OK` (`UserResponse` with tokens)

### 3.4 Client Token Refresh (`POST /api/client/auth/refresh`)
- **Authority / Permission**: Authenticated via cookie or request body
- **Method**: `POST`
- **Path**: `/api/client/auth/refresh`
- **Response**: `200 OK`

### 3.5 Client Logout (`POST /api/client/auth/logout`)
- **Authority / Permission**: None (🟢 **PUBLIC**)
- **Method**: `POST`
- **Path**: `/api/client/auth/logout`
- **Cookies Cleared**: `access_token`, `refresh_token`
- **Response**: `200 OK`

### 3.6 Client Current User Profile (`GET /api/client/auth/me`)
- **Authority / Permission**: Authenticated
- **Method**: `GET`
- **Path**: `/api/client/auth/me`
- **Response**: `200 OK` (`UserResponse`)

### 3.7 User Registration Endpoint (`POST /api/client/users/register`)
- **Authority / Permission**: None (🟢 **PUBLIC**)
- **Method**: `POST`
- **Path**: `/api/client/users/register`
- **Request Body**:
  ```json
  {
    "name": "Jane Doe",
    "email": "jane.doe@example.com",
    "password": "SecurePassword123!",
    "phone": "+1234567890"
  }
  ```
- **Response**: `200 OK` (`UserResponse`)

### 3.8 Client Read Current Profile (`GET /api/client/users/me`)
- **Authority / Permission**: Authenticated
- **Method**: `GET`
- **Path**: `/api/client/users/me`
- **Response**: `200 OK` (`UserResponse`)

### 3.9 Client Read User by ID (`GET /api/client/users/{user_id}`)
- **Authority / Permission**: Authenticated
- **Method**: `GET`
- **Path**: `/api/client/users/{user_id}`
- **Authorization & Usecase Validation**: Client users can **only read their own data**. Attempting to read another user returns `403 Forbidden`.
- **Response**: `200 OK` (`UserResponse`)

### 3.10 Client Update User by ID (`PUT /api/client/users/{user_id}`)
- **Authority / Permission**: Authenticated
- **Method**: `PUT`
- **Path**: `/api/client/users/{user_id}`
- **Request Body**:
  ```json
  {
    "name": "Jane Doe Updated",
    "email": "jane.updated@example.com",
    "password": "NewSecurePassword123!",
    "phone": "+1987654321"
  }
  ```
- **Restrictions**: Client users cannot update `role` or `active` status.
- **Authorization & Usecase Validation**: Client users can **only update their own data**. Attempting to update another user returns `403 Forbidden`.
- **Response**: `200 OK` (`UserResponse`)

### 3.11 Create Match Request (`POST /api/client/match-requests`)
- **Authority / Permission**: Authenticated (🔑 **AUTHENTICATED**)
- **Method**: `POST`
- **Path**: `/api/client/match-requests`
- **Description**: Creates a new match request looking for opponents or teammates. Only logged-in users can create requests. `requested_by` is automatically set to the authenticated user.
- **Request Body (`MatchRequestCreateDto`)**:
  ```json
  {
    "game": "Futsal",
    "request_type": "OPPONENT",
    "category": "5v5 Casual",
    "message": "Looking for a competitive 5v5 team tonight!",
    "time": "2026-10-28T19:00:00",
    "urgent": true
  }
  ```
- **Fields & Validations (`MatchRequestCreateDto`)**:
  - `game`: string, **required**, `@NotBlank`, `@Size(max = 100)`
  - `request_type`: string, **required**, `@NotNull`, `@Pattern("OPPONENT|TEAMMATE")`
  - `category`: string, optional, `@Size(max = 100)`
  - `message`: string, optional, `@Size(max = 2000)`
  - `time`: string (ISO-8601 date-time), optional scheduled match time
  - `urgent`: boolean, optional (defaults to `false`)
- **Response**: `200 OK`
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": {
      "id": "e8334460-aefb-4171-872f-5ea3b1187c32",
      "requested_by": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "request_type": "OPPONENT",
      "status": "OPEN",
      "game": "Futsal",
      "category": "5v5 Casual",
      "message": "Looking for a competitive 5v5 team tonight!",
      "time": "2026-10-28T19:00:00",
      "urgent": true,
      "created_on": "2026-10-28T14:40:00",
      "modified_on": "2026-10-28T14:40:00",
      "created_by": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "modified_by": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "active": true,
      "deleted": false
    },
    "time": "2026-10-28T14:40:00.123456"
  }
  ```

### 3.12 Get Match Request by ID (`GET /api/client/match-requests/{id}`)
- **Authority / Permission**: Authenticated (🔑 **AUTHENTICATED**)
- **Method**: `GET`
- **Path**: `/api/client/match-requests/{id}`
- **Response**: `200 OK` (`MatchRequestResponseDto`)

### 3.13 Update Match Request by ID (`PUT /api/client/match-requests/{id}`)
- **Authority / Permission**: Authenticated (🔑 **AUTHENTICATED**)
- **Method**: `PUT`
- **Path**: `/api/client/match-requests/{id}`
- **Authorization & Usecase Validation**: Only the user who **created** the request can update it. Any other user attempting to update it receives `403 Forbidden` (`"Access denied: You can only update match requests you have created"`).
- **Request Body (`MatchRequestUpdateDto`)**:
  ```json
  {
    "game": "Futsal",
    "request_type": "OPPONENT",
    "status": "OPEN",
    "category": "5v5 Friendly",
    "message": "Updated match notes",
    "time": "2026-10-28T20:00:00",
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

### 3.14 Delete Match Request by ID (`DELETE /api/client/match-requests/{id}`)
- **Authority / Permission**: Authenticated (🔑 **AUTHENTICATED**)
- **Method**: `DELETE`
- **Path**: `/api/client/match-requests/{id}`
- **Authorization & Usecase Validation**: Only the user who **created** the request can delete it. Any other user attempting to delete it receives `403 Forbidden` (`"Access denied: You can only delete match requests you have created"`).
- **Response**: `200 OK`
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": null,
    "time": "2026-10-28T14:45:00.123456"
  }
  ```

### 3.15 Search Match Requests (`POST /api/client/match-requests/search`)
- **Authority / Permission**: Authenticated (🔑 **AUTHENTICATED**)
- **Method**: `POST`
- **Path**: `/api/client/match-requests/search`
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
          "category": "5v5 Casual",
          "message": "Looking for a competitive 5v5 team tonight!",
          "time": "2026-10-28T19:00:00",
          "urgent": true,
          "created_on": "2026-10-28T14:40:00",
          "modified_on": "2026-10-28T14:40:00",
          "active": true,
          "deleted": false
        }
      ]
    },
    "time": "2026-10-28T14:45:00.123456"
  }
  ```

---

## 4. System Utility & Infrastructure APIs

### 4.1 Orchestrator Health Check (`/health`)
- **Authority / Permission**: None (🟢 **PUBLIC**)
- **Method**: `GET`
- **Path**: `/health` (available on port 8021 and 8022)
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
    "time": "2026-10-22T09:30:00.123456"
  }
  ```
