# Client Portal Frontend API Contract & Integration Guide

This document is the single source of truth for Frontend (FE) client applications (store, merchant, customer, player, mobile apps) communicating with the **Client Portal API** service on port **8022** (`/api/client`).

> [!IMPORTANT]
> **RULE BOOK REQUIREMENT**: Every time a client endpoint, request parameter, or response field is added or modified, this file **MUST** be updated immediately in the same commit/change.

---

## 1. General Architecture & Envelopes

### Base URLs & Service Discovery
- **Client Portal API Base URL**: `http://localhost:8022/api/client`
- **Interactive Swagger Documentation**: [`http://localhost:8022/swagger-ui.html`](http://localhost:8022/swagger-ui.html)
- **OpenAPI Schema (JSON)**: [`http://localhost:8022/v3/api-docs`](http://localhost:8022/v3/api-docs)
- **Frontend Vite Dev Proxy**: `http://localhost:5173` (proxies `/api/client` → `http://localhost:8022`)

### Global Response Envelope (`ResponseTemplate<T>`)
Every JSON response from the Client Portal backend is wrapped in the standard `ResponseTemplate<T>` matching `bazar-backend`:

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

### Authentication & Security Standards
- **Authentication Channels**:
  - **HttpOnly Cookies**: `access_token` and `refresh_token`. Automatically included by browser requests.
  - **Bearer Header**: `Authorization: Bearer <token>` for non-browser / mobile clients.
- **CORS & Credentials**:
  - Frontend fetch / axios requests **MUST** include credentials (`credentials: 'include'` in `fetch` or `withCredentials: true` in `axios`).
  - Allowed origins in development: `http://localhost:3000`, `http://localhost:5173`, `http://localhost:8000`, `http://localhost:8021`, `http://localhost:8022`.

---

## 2. Health & Diagnostics

### 2.1 Health Check (`GET /health`)
- **Authority / Permission**: None (🟢 **PUBLIC**)
- **Method**: `GET`
- **Path**: `/health`
- **Port**: 8022
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

### 2.2 Client Ping (`GET /api/client/ping`)
- **Authority / Permission**: None (🟢 **PUBLIC**)
- **Method**: `GET`
- **Path**: `/api/client/ping`
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

## 3. Client Authentication & Session Management (`/api/client/auth`)

### 3.1 Client Login (`POST /api/client/auth/login`)
- **Authority / Permission**: None (🟢 **PUBLIC**)
- **Method**: `POST`
- **Path**: `/api/client/auth/login`
- **Request Body (`LoginRequest`)**:
  ```json
  {
    "email": "customer@example.com",
    "password": "CustomerPassword123!"
  }
  ```
- **Cookies Set**:
  - `access_token`: HttpOnly; Path=/; Max-Age=86400 (24 hours); SameSite=Lax
  - `refresh_token`: HttpOnly; Path=/; Max-Age=604800 (7 days); SameSite=Lax
- **Errors**:
  - `401 Unauthorized`: Invalid email or password (`{"code": "401", "message": "Invalid email or password"}`) or inactive account (`{"code": "401", "message": "User account is inactive"}`).
  - `429 Too Many Requests`: Exceeded 5 failed attempts within 1 minute.
- **Response**: `200 OK`
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": {
      "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "name": "Jane Doe",
      "email": "customer@example.com",
      "phone": "+1234567890",
      "role": "CLIENT",
      "roles": [
        {
          "id": "c1234567-89ab-cdef-0123-456789abcdef",
          "user_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
          "role": "CLIENT",
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

### 3.2 Client Registration (`POST /api/client/auth/register`)
- **Authority / Permission**: None (🟢 **PUBLIC**)
- **Method**: `POST`
- **Path**: `/api/client/auth/register`
- **Request Body (`UserRegisterDto`)**:
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
- **Response**: `200 OK` (`UserResponse` with tokens, matching login format).

### 3.3 Client Token Refresh (`POST /api/client/auth/refresh`)
- **Authority / Permission**: Authenticated via `refresh_token` cookie or request body
- **Method**: `POST`
- **Path**: `/api/client/auth/refresh`
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

### 3.4 Client Logout (`POST /api/client/auth/logout`)
- **Authority / Permission**: None (🟢 **PUBLIC**)
- **Method**: `POST`
- **Path**: `/api/client/auth/logout`
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

### 3.5 Client Current User Profile (`GET /api/client/auth/me`)
- **Authority / Permission**: Authenticated (🔑 **AUTHENTICATED**)
- **Method**: `GET`
- **Path**: `/api/client/auth/me`
- **Response**: `200 OK` (`UserResponse`)

---

## 4. User Self-Management (`/api/client/users`)

### 4.1 User Registration Endpoint (`POST /api/client/users/register`)
- **Authority / Permission**: None (🟢 **PUBLIC**)
- **Method**: `POST`
- **Path**: `/api/client/users/register`
- **Request Body (`UserRegisterDto`)**:
  ```json
  {
    "name": "Jane Doe",
    "email": "jane.doe@example.com",
    "password": "SecurePassword123!",
    "phone": "+1234567890"
  }
  ```
- **Response**: `200 OK` (`UserResponse`)

### 4.2 Client Read Current Profile (`GET /api/client/users/me`)
- **Authority / Permission**: Authenticated (🔑 **AUTHENTICATED**)
- **Method**: `GET`
- **Path**: `/api/client/users/me`
- **Response**: `200 OK` (`UserResponse`)

### 4.3 Client Read User by ID (`GET /api/client/users/{user_id}`)
- **Authority / Permission**: Authenticated (🔑 **AUTHENTICATED**)
- **Method**: `GET`
- **Path**: `/api/client/users/{user_id}`
- **Path Parameters**: `user_id` (UUID string)
- **Authorization & Usecase Validation**: Client users can **only read their own data**. Attempting to read another user returns `403 Forbidden` (`"Access denied: You can only access your own profile"`).
- **Response**: `200 OK` (`UserResponse`)

### 4.4 Client Update User by ID (`PUT /api/client/users/{user_id}`)
- **Authority / Permission**: Authenticated (🔑 **AUTHENTICATED**)
- **Method**: `PUT`
- **Path**: `/api/client/users/{user_id}`
- **Path Parameters**: `user_id` (UUID string)
- **Request Body (`UserUpdateDto`)**:
  ```json
  {
    "name": "Jane Doe Updated",
    "email": "jane.updated@example.com",
    "password": "NewSecurePassword123!",
    "phone": "+1987654321"
  }
  ```
- **Restrictions**: Client users cannot update `role` or `active` status.
- **Authorization & Usecase Validation**: Client users can **only update their own data**. Attempting to update another user returns `403 Forbidden` (`"Access denied: You can only update your own profile"`).
- **Response**: `200 OK` (`UserResponse`)

---

## 5. Match Requests (`/api/client/match-requests`)

Used by customers/players to find opponents or teammates for sports and gaming sessions.

### 5.1 Create Match Request (`POST /api/client/match-requests`)
- **Authority / Permission**: Authenticated (🔑 **AUTHENTICATED**)
- **Method**: `POST`
- **Path**: `/api/client/match-requests`
- **Description**: Creates a new match request looking for opponents or teammates. Only logged-in users can create requests. `requested_by` is automatically set to the authenticated user's ID.
- **Request Body (`MatchRequestCreateDto`)**:
  ```json
  {
    "game": "Futsal",
    "request_type": "OPPONENT",
    "category": "5v5 Casual",
    "message": "Looking for a competitive 5v5 team tonight!",
    "time": "2026-10-30T19:00:00",
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
      "time": "2026-10-30T19:00:00",
      "urgent": true,
      "created_on": "2026-10-30T14:40:00",
      "modified_on": "2026-10-30T14:40:00",
      "created_by": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "modified_by": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "active": true,
      "deleted": false
    },
    "time": "2026-10-30T14:40:00.123456"
  }
  ```

### 5.2 Get Match Request by ID (`GET /api/client/match-requests/{id}`)
- **Authority / Permission**: Authenticated (🔑 **AUTHENTICATED**)
- **Method**: `GET`
- **Path**: `/api/client/match-requests/{id}`
- **Path Parameters**: `id` (UUID string)
- **Response**: `200 OK` (`MatchRequestResponseDto`)

### 5.3 Update Match Request by ID (`PUT /api/client/match-requests/{id}`)
- **Authority / Permission**: Authenticated (🔑 **AUTHENTICATED**)
- **Method**: `PUT`
- **Path**: `/api/client/match-requests/{id}`
- **Path Parameters**: `id` (UUID string)
- **Authorization & Usecase Validation**: Only the user who **created** the request can update it. Any other user attempting to update it receives `403 Forbidden` (`"Access denied: You can only update match requests you have created"`).
- **Request Body (`MatchRequestUpdateDto`)**:
  ```json
  {
    "game": "Futsal",
    "request_type": "OPPONENT",
    "status": "OPEN",
    "category": "5v5 Friendly",
    "message": "Updated match notes",
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

### 5.4 Delete Match Request by ID (`DELETE /api/client/match-requests/{id}`)
- **Authority / Permission**: Authenticated (🔑 **AUTHENTICATED**)
- **Method**: `DELETE`
- **Path**: `/api/client/match-requests/{id}`
- **Path Parameters**: `id` (UUID string)
- **Authorization & Usecase Validation**: Only the user who **created** the request can delete it. Any other user attempting to delete it receives `403 Forbidden` (`"Access denied: You can only delete match requests you have created"`).
- **Response**: `200 OK`
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": null,
    "time": "2026-10-30T14:45:00.123456"
  }
  ```

### 5.5 Search Match Requests (`POST /api/client/match-requests/search`)
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
