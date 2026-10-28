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
  - `access_token`: HttpOnly; Path=/; Max-Age=900 (15 min); SameSite=Lax
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
