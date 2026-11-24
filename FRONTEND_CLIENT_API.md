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

An unexpected server error returns HTTP 500 with this envelope. The `X-Request-ID` response header identifies the request in the server logs.

```json
{
  "code": "500",
  "message": "Internal Server Error",
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
  - `401 Unauthorized`: Invalid email or password (`{"code": "401", "message": "Invalid email or password"}`), inactive account (`{"code": "401", "message": "User account is inactive"}`), or unverified account (`{"code": "401", "message": "Account is not verified. Please verify your OTP."}`).
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
      "verified": true,
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
- **Request Body (`RegisterRequest`)**:
  ```json
  {
    "name": "Jane Doe",
    "email": "jane.doe@example.com",
    "password": "SecurePassword123!",
    "phone": "+1234567890"
  }
  ```
- **Verification Workflow**:
  - Automatically creates client account with `verified: false`.
  - Generates a 6-digit numeric OTP and dispatches it to the client's email (logged in development mode).
  - No auth cookies are set at this stage. Account must be confirmed via `POST /api/client/auth/verify-otp`.
- **Response**: `200 OK` (`UserResponse` without tokens, `verified: false`).
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": {
      "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "name": "Jane Doe",
      "email": "jane.doe@example.com",
      "phone": "+1234567890",
      "role": "CLIENT",
      "roles": [
        {
          "id": "c1234567-89ab-cdef-0123-456789abcdef",
          "user_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
          "role": "CLIENT",
          "created_on": "2026-10-31T08:00:00Z",
          "modified_on": "2026-10-31T08:00:00Z",
          "active": true,
          "deleted": false
        }
      ],
      "verified": false,
      "created_on": "2026-10-31T08:00:00Z",
      "modified_on": "2026-10-31T08:00:00Z",
      "active": true,
      "deleted": false
    },
    "time": "2026-10-31T08:00:00.123456"
  }
  ```

### 3.3 Client Verify OTP (`POST /api/client/auth/verify-otp`)
- **Authority / Permission**: None (🟢 **PUBLIC**)
- **Method**: `POST`
- **Path**: `/api/client/auth/verify-otp`
- **Request Body (`VerifyOtpRequest`)**:
  ```json
  {
    "email": "jane.doe@example.com",
    "otp": "481923"
  }
  ```
- **Validation & Business Rules**:
  - `email` must be a valid email format.
  - `otp` must be a 6-digit number string (`^[0-9]{6}$`).
  - Maximum 5 attempts allowed before OTP is invalidated.
  - Validates OTP within its 5-minute validity window.
  - On success, marks user `verified: true` and issues JWT access and refresh cookies.
- **Cookies Set**:
  - `access_token`: HttpOnly; Path=/; Max-Age=86400 (24 hours); SameSite=Lax
  - `refresh_token`: HttpOnly; Path=/; Max-Age=604800 (7 days); SameSite=Lax
- **Errors**:
  - `400 Bad Request`: Invalid OTP (`{"code": "400", "message": "Invalid OTP. 4 attempt(s) remaining."}`), expired OTP (`{"code": "400", "message": "OTP has expired. Please request a new OTP."}`), maximum attempts exceeded (`{"code": "400", "message": "Maximum verification attempts exceeded. Please request a new OTP."}`), or already verified (`{"code": "400", "message": "Account is already verified"}`).
  - `404 Not Found`: User not found with email.
- **Response**: `200 OK` (`UserResponse` with tokens, `verified: true`).
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": {
      "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "name": "Jane Doe",
      "email": "jane.doe@example.com",
      "phone": "+1234567890",
      "role": "CLIENT",
      "roles": [
        {
          "id": "c1234567-89ab-cdef-0123-456789abcdef",
          "user_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
          "role": "CLIENT",
          "created_on": "2026-10-31T08:00:00Z",
          "modified_on": "2026-10-31T08:00:00Z",
          "active": true,
          "deleted": false
        }
      ],
      "verified": true,
      "access_token": "<jwt-token>",
      "refresh_token": "<jwt-token>",
      "token_type": "Bearer",
      "created_on": "2026-10-31T08:00:00Z",
      "modified_on": "2026-10-31T08:00:00Z",
      "active": true,
      "deleted": false
    },
    "time": "2026-10-31T08:00:00.123456"
  }
  ```

### 3.4 Client Resend OTP (`POST /api/client/auth/resend-otp`)
- **Authority / Permission**: None (🟢 **PUBLIC**)
- **Method**: `POST`
- **Path**: `/api/client/auth/resend-otp`
- **Request Body (`ResendOtpRequest`)**:
  ```json
  {
    "email": "jane.doe@example.com"
  }
  ```
- **Cooldown & Throttling**:
  - Enforces a 60-second cooldown period between resend attempts.
  - Invalidates any previous unconsumed OTPs for the user upon generation of the new OTP.
- **Errors**:
  - `400 Bad Request`: Cooldown still active (`{"code": "400", "message": "Please wait 45 seconds before requesting a new OTP."}`) or account already verified (`{"code": "400", "message": "Account is already verified"}`).
  - `404 Not Found`: User not found with email.
- **Response**: `200 OK`
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": {
      "message": "OTP resent successfully"
    },
    "time": "2026-10-31T08:00:00.123456"
  }
  ```

### 3.5 Client Token Refresh (`POST /api/client/auth/refresh`)
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

### 3.6 Client Logout (`POST /api/client/auth/logout`)
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

### 3.7 Client Current User Profile (`GET /api/client/auth/me`)
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

> [!IMPORTANT]
> **DATABASE MASTER DATA VALIDATION RULE**:
> All `category` and `game` values provided when creating or updating a match request **MUST exist in the database's master data** (`system.master_data` and `system.master_data_items`) created by system users:
> 1. **`category` validation**: Must match an active category in master data (e.g. `SPORTS`, `E_SPORTS`, `TRAVEL`, or any custom category added by system admins). Matches by code, title, or UUID.
> 2. **`game` validation**: Must match an active game/activity defined under that specific category in master data (e.g. `Futsal` under `SPORTS`; `Valorant` under `E_SPORTS`; `Hiking` under `TRAVEL`). Cross-category submissions (e.g. `category: "SPORTS"` with `game: "Valorant"`) will fail with an HTTP 400 error.
> 3. Frontend applications should always populate Category and Game selectors dynamically from the Master Data / Categories APIs (`GET /api/client/categories/tree` or `GET /api/client/categories/{code}/items`).

### 5.1 Create Match Request (`POST /api/client/match-requests`)
- **Authority / Permission**: Authenticated (🔑 **AUTHENTICATED**)
- **Method**: `POST`
- **Path**: `/api/client/match-requests`
- **Description**: Creates a new match request looking for opponents or teammates. Only logged-in users can create requests. `requested_by` is automatically set to the authenticated user's ID. When looking for teammates (`request_type` = `TEAMMATE`), you can specify the number of players needed via `players_needed` (defaults to 1). For opponent challenges (`request_type` = `OPPONENT`), `players_needed` is automatically set to null.
- **Request Body (`MatchRequestCreateDto`)**:
  ```json
  {
    "category": "SPORTS",
    "game": "Futsal",
    "request_type": "TEAMMATE",
    "message": "Looking for 2 players to join our futsal team tonight!",
    "time": "2026-11-08T19:00:00",
    "location": "Dillibazar Futsal Arena, Kathmandu",
    "players_needed": 2,
    "urgent": true
  }
  ```
- **Fields & Validations (`MatchRequestCreateDto`)**:
  - `category`: string, **required**, `@Size(max = 100)`. Must exist in DB master data under `CATEGORY` (e.g. `"SPORTS"`, `"E_SPORTS"`, `"TRAVEL"`).
  - `game`: string, **required**, `@NotBlank`, `@Size(max = 100)`. Must be a valid active game/activity in DB master data under the selected `category`.
  - `request_type`: string, **required**, `@NotNull`, `@Pattern("OPPONENT|TEAMMATE")`
  - `message`: string, optional, `@Size(max = 2000)`
  - `time`: string (ISO-8601 date-time), optional scheduled match time
  - `location`: string, optional, `@Size(max = 255)`
  - `players_needed`: integer, optional (`@Min(1)`, `@Max(100)`). Number of players wanted when `request_type` is `TEAMMATE` (defaults to 1 if omitted). Ignored/null for `OPPONENT` requests. Also accepts aliases `player_count`, `players_count`, `playersNeeded`.
  - `urgent`: boolean, optional (defaults to `false`)
- **Validation Errors (`HTTP 400 Bad Request`)**:
  - If `category` is invalid or not in master data:
    ```json
    {
      "code": "400",
      "message": "Invalid category: '5v5 Casual'. Category must be an active category defined in master data.",
      "data": null,
      "time": "2026-11-08T10:30:00.123456"
    }
    ```
  - If `game` does not exist under the specified `category` in master data:
    ```json
    {
      "code": "400",
      "message": "Invalid game: 'Valorant'. Game must be a valid active game/activity under category 'SPORTS' from master data.",
      "data": null,
      "time": "2026-11-08T10:30:00.123456"
    }
    ```
- **Response**: `200 OK`
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": {
      "id": "e8334460-aefb-4171-872f-5ea3b1187c32",
      "requested_by": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "request_type": "TEAMMATE",
      "status": "OPEN",
      "category": "SPORTS",
      "game": "Futsal",
      "message": "Looking for 2 players to join our futsal team tonight!",
      "time": "2026-11-08T19:00:00",
      "location": "Dillibazar Futsal Arena, Kathmandu",
      "players_needed": 2,
      "urgent": true,
      "created_on": "2026-11-08T10:30:00",
      "modified_on": "2026-11-08T10:30:00",
      "created_by": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "modified_by": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "active": true,
      "deleted": false
    },
    "time": "2026-11-08T10:30:00.123456"
  }
  ```

### 5.2 Get Match Request by ID (`GET /api/client/match-requests/{id}`)
- **Authority / Permission**: Authenticated (🔑 **AUTHENTICATED**)
- **Method**: `GET`
- **Path**: `/api/client/match-requests/{id}`
- **Path Parameters**: `id` (UUID string)
- **Authorization & Visibility Rule**: Users only get to see match requests that are in the future (`time` is in the future or not specified). Only the user who posted/created the request can view their own past match requests. If a user attempts to retrieve another user's past match request, the API responds with `403 Forbidden` (`"Access denied: You cannot view past match requests posted by other users"`). System administrators can view any match request.
- **Response**: `200 OK` (`MatchRequestResponseDto`)

### 5.3 Update Match Request by ID (`PUT /api/client/match-requests/{id}`)
- **Authority / Permission**: Authenticated (🔑 **AUTHENTICATED**)
- **Method**: `PUT`
- **Path**: `/api/client/match-requests/{id}`
- **Path Parameters**: `id` (UUID string)
- **Authorization & Usecase Validation**:
  - Only the user who **created** the request can update it. Any other user attempting to update it receives `403 Forbidden` (`"Access denied: You can only update match requests you have created"`).
  - If `category` or `game` are updated, they are re-validated against active database master data (same rules as creation). Submitting an unknown category or cross-category game returns `400 Bad Request`.
- **Request Body (`MatchRequestUpdateDto`)**:
  ```json
  {
    "category": "SPORTS",
    "game": "Futsal",
    "request_type": "TEAMMATE",
    "status": "OPEN",
    "message": "Updated match notes: need 2 more players now",
    "time": "2026-11-08T20:00:00",
    "location": "Dillibazar Futsal Arena, Kathmandu",
    "players_needed": 2,
    "urgent": false
  }
  ```
- **Fields & Validations (`MatchRequestUpdateDto`)**:
  - `category`: string, optional, `@Size(max = 100)`. If provided, must match an active category in master data.
  - `game`: string, optional, `@Size(max = 100)`. If provided, must match an active game/activity under the resolved category in master data.
  - `request_type`: string, optional, `@Pattern("OPPONENT|TEAMMATE")`
  - `status`: string, optional, `@Pattern("OPEN|MATCHED|FILLED|CANCELLED|COMPLETED")`
  - `message`: string, optional, `@Size(max = 2000)`
  - `time`: string (ISO-8601 date-time), optional
  - `location`: string, optional, `@Size(max = 255)`
  - `players_needed`: integer, optional (`@Min(1)`, `@Max(100)`). Number of players needed for teammate requests.
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
- **Visibility & Access Rules**:
  - **Future vs. Past Requests**: Users only get to see match requests that are in the future (`time` is in the future or not specified).
  - **Own Past Requests**: Only the user who posted a match request can see their own past requests. Other users' past requests are automatically filtered out.
  - **System Administrators**: Administrative accounts can view all match requests without time restriction.
- **Request Body (`MatchRequestFilter`)**:
  ```json
  {
    "pageNumber": 1,
    "pageSize": 10,
    "sortField": "created_on",
    "sortOrder": "DESC",
    "game": "Futsal",
    "requestType": "TEAMMATE",
    "status": "OPEN",
    "location": "Kathmandu",
    "players_needed": 3,
    "urgent": true,
    "requestedBy": "optional-user-id",
    "upcomingOnly": false,
    "pastOnly": false
  }
  ```
- **Fields & Validations (`MatchRequestFilter`)**:
  - `pageNumber`: integer, page number (1-indexed, default 1)
  - `pageSize`: integer, page size limit (default 10)
  - `sortField`: string, sort column (`created_on`, `time`, `game`, `category`, `location`, `players_needed`, `status`, `request_type`, `urgent`)
  - `sortOrder`: string (`ASC` or `DESC`)
  - `game`: string, optional partial match
  - `requestType`: string, optional (`OPPONENT` or `TEAMMATE`)
  - `status`: string, optional (`OPEN`, `MATCHED`, `FILLED`, `CANCELLED`, `COMPLETED`)
  - `location`: string, optional partial match
  - `players_needed`: integer, optional filter for required player count
  - `urgent`: boolean, optional
  - `requestedBy`: string (UUID), optional user filter
  - `timeFrom`: string (ISO-8601 date-time), optional filter for match time >= timeFrom
  - `timeTo`: string (ISO-8601 date-time), optional filter for match time <= timeTo
  - `upcomingOnly`: boolean, optional. When `true`, filters only future/upcoming match requests.
  - `pastOnly`: boolean, optional. When `true`, filters only past match requests (returns only the authenticated user's own past requests).
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
          "request_type": "TEAMMATE",
          "status": "OPEN",
          "game": "Futsal",
          "category": "5v5 Casual",
          "message": "Looking for 3 players to join our futsal team tonight!",
          "time": "2026-10-30T19:00:00",
          "location": "Dillibazar Futsal Arena, Kathmandu",
          "players_needed": 3,
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

---

## 6. Categories (`/api/client/categories`)

Provides predefined sports, gaming, and activity categories for categorizing match requests and events, linking directly to master data items (games/activities).

### 6.1 List All Categories (`GET /api/client/categories`)
- **Authority / Permission**: Public (🌐 **PUBLIC**)
- **Method**: `GET`
- **Path**: `/api/client/categories`
- **Query Parameters**:
  - `includeItems` (boolean, optional, default: `false`): If `true`, returns all items (games/activities) nested inside each category.
- **Description**: Returns all available categories (`E_SPORTS`, `SPORTS`, `TRAVEL`).
- **Response**: `200 OK`
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": [
      {
        "code": "E_SPORTS",
        "name": "E-Sports",
        "slug": "e-sports",
        "description": "Electronic sports and competitive video gaming",
        "master_data_code": "ESPORTS_GAMES"
      },
      {
        "code": "SPORTS",
        "name": "Sports",
        "slug": "sports",
        "description": "Physical outdoor and indoor sports",
        "master_data_code": "SPORTS_GAMES"
      },
      {
        "code": "TRAVEL",
        "name": "Travel",
        "slug": "travel",
        "description": "Travel, outdoor adventures, and tours",
        "master_data_code": "TRAVEL_ACTIVITIES"
      }
    ],
    "time": "2026-11-07T10:55:00.123456"
  }
  ```

### 6.2 Get Category Tree with Items (`GET /api/client/categories/tree` or `?includeItems=true`)
- **Authority / Permission**: Public (🌐 **PUBLIC**)
- **Method**: `GET`
- **Path**: `/api/client/categories/tree`
- **Description**: Returns full category tree with all games/activities nested under each category in a single request.
- **Response**: `200 OK`
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": [
      {
        "code": "E_SPORTS",
        "name": "E-Sports",
        "slug": "e-sports",
        "description": "Electronic sports and competitive video gaming",
        "master_data_code": "ESPORTS_GAMES",
        "items": [
          {
            "id": "c3000000-0000-0000-0000-000000000002",
            "master_data_id": "b1000000-0000-0000-0000-000000000003",
            "title": "Counter-Strike 2",
            "code": "CS2",
            "description": "Competitive FPS shooter",
            "active": true
          },
          {
            "id": "c3000000-0000-0000-0000-000000000003",
            "master_data_id": "b1000000-0000-0000-0000-000000000003",
            "title": "Dota 2",
            "code": "DOTA2",
            "description": "MOBA team strategy game",
            "active": true
          },
          {
            "id": "c3000000-0000-0000-0000-000000000005",
            "master_data_id": "b1000000-0000-0000-0000-000000000003",
            "title": "Mobile Legends",
            "code": "MLBB",
            "description": "5v5 mobile MOBA",
            "active": true
          },
          {
            "id": "c3000000-0000-0000-0000-000000000004",
            "master_data_id": "b1000000-0000-0000-0000-000000000003",
            "title": "PUBG Mobile",
            "code": "PUBG_MOBILE",
            "description": "Battle royale survival shooter",
            "active": true
          },
          {
            "id": "c3000000-0000-0000-0000-000000000001",
            "master_data_id": "b1000000-0000-0000-0000-000000000003",
            "title": "Valorant",
            "code": "VALORANT",
            "description": "5v5 tactical character-based shooter",
            "active": true
          }
        ]
      },
      {
        "code": "SPORTS",
        "name": "Sports",
        "slug": "sports",
        "description": "Physical outdoor and indoor sports",
        "master_data_code": "SPORTS_GAMES",
        "items": [
          {
            "id": "c2000000-0000-0000-0000-000000000005",
            "master_data_id": "b1000000-0000-0000-0000-000000000002",
            "title": "Badminton",
            "code": "BADMINTON",
            "description": "Singles and doubles badminton",
            "active": true
          },
          {
            "id": "c2000000-0000-0000-0000-000000000003",
            "master_data_id": "b1000000-0000-0000-0000-000000000002",
            "title": "Basketball",
            "code": "BASKETBALL",
            "description": "Full-court or 3v3 basketball",
            "active": true
          },
          {
            "id": "c2000000-0000-0000-0000-000000000004",
            "master_data_id": "b1000000-0000-0000-0000-000000000002",
            "title": "Cricket",
            "code": "CRICKET",
            "description": "Box cricket, T20, and outdoor cricket",
            "active": true
          },
          {
            "id": "c2000000-0000-0000-0000-000000000002",
            "master_data_id": "b1000000-0000-0000-0000-000000000002",
            "title": "Football",
            "code": "FOOTBALL",
            "description": "Standard 11v11 association football",
            "active": true
          },
          {
            "id": "c2000000-0000-0000-0000-000000000001",
            "master_data_id": "b1000000-0000-0000-0000-000000000002",
            "title": "Futsal",
            "code": "FUTSAL",
            "description": "5v5 or 6v6 indoor/outdoor futsal",
            "active": true
          }
        ]
      },
      {
        "code": "TRAVEL",
        "name": "Travel",
        "slug": "travel",
        "description": "Travel, outdoor adventures, and tours",
        "master_data_code": "TRAVEL_ACTIVITIES",
        "items": [
          {
            "id": "c4000000-0000-0000-0000-000000000003",
            "master_data_id": "b1000000-0000-0000-0000-000000000004",
            "title": "Camping",
            "code": "CAMPING",
            "description": "Outdoor camping and night stargazing",
            "active": true
          },
          {
            "id": "c4000000-0000-0000-0000-000000000004",
            "master_data_id": "b1000000-0000-0000-0000-000000000004",
            "title": "Cycling",
            "code": "CYCLING",
            "description": "Road and mountain biking trips",
            "active": true
          },
          {
            "id": "c4000000-0000-0000-0000-000000000001",
            "master_data_id": "b1000000-0000-0000-0000-000000000004",
            "title": "Hiking",
            "code": "HIKING",
            "description": "Day hikes and hill trails",
            "active": true
          },
          {
            "id": "c4000000-0000-0000-0000-000000000002",
            "master_data_id": "b1000000-0000-0000-0000-000000000004",
            "title": "Trekking",
            "code": "TREKKING",
            "description": "Multi-day mountain and trail expeditions",
            "active": true
          }
        ]
      }
    ],
    "time": "2026-11-07T10:55:00.123456"
  }
  ```

### 6.3 Get Category by Code or Slug (`GET /api/client/categories/{code}`)
- **Authority / Permission**: Public (🌐 **PUBLIC**)
- **Method**: `GET`
- **Path**: `/api/client/categories/{code}`
- **Path Parameters**: `code` (string, case-insensitive match by code e.g. `SPORTS`, `sports`, `e-sports`)
- **Response**: `200 OK`
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": {
      "code": "SPORTS",
      "name": "Sports",
      "slug": "sports",
      "description": "Physical outdoor and indoor sports",
      "master_data_code": "SPORTS_GAMES"
    },
    "time": "2026-11-07T10:55:00.123456"
  }
  ```

### 6.4 Get Items / Games for a Category (`GET /api/client/categories/{code}/items`)
- **Authority / Permission**: Public (🌐 **PUBLIC**)
- **Method**: `GET`
- **Path**: `/api/client/categories/{code}/items`
- **Path Parameters**: `code` (string, category code or slug e.g. `SPORTS`, `sports`, `e-sports`, `travel`)
- **Description**: Returns all games or activities under that category.
- **Response**: `200 OK`
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": [
      {
        "id": "c2000000-0000-0000-0000-000000000005",
        "master_data_id": "b1000000-0000-0000-0000-000000000002",
        "title": "Badminton",
        "code": "BADMINTON",
        "description": "Singles and doubles badminton",
        "active": true
      },
      {
        "id": "c2000000-0000-0000-0000-000000000003",
        "master_data_id": "b1000000-0000-0000-0000-000000000002",
        "title": "Basketball",
        "code": "BASKETBALL",
        "description": "Full-court or 3v3 basketball",
        "active": true
      },
      {
        "id": "c2000000-0000-0000-0000-000000000004",
        "master_data_id": "b1000000-0000-0000-0000-000000000002",
        "title": "Cricket",
        "code": "CRICKET",
        "description": "Box cricket, T20, and outdoor cricket",
        "active": true
      },
      {
        "id": "c2000000-0000-0000-0000-000000000002",
        "master_data_id": "b1000000-0000-0000-0000-000000000002",
        "title": "Football",
        "code": "FOOTBALL",
        "description": "Standard 11v11 association football",
        "active": true
      },
      {
        "id": "c2000000-0000-0000-0000-000000000001",
        "master_data_id": "b1000000-0000-0000-0000-000000000002",
        "title": "Futsal",
        "code": "FUTSAL",
        "description": "5v5 or 6v6 indoor/outdoor futsal",
        "active": true
      }
    ],
    "time": "2026-11-07T10:55:00.123456"
  }
  ```

---

## 7. Master Data (`/api/client/master-data`)

Read-only reference endpoints for retrieving system master data headers and their corresponding items.

### 7.1 Get Master Data by ID (`GET /api/client/master-data/{id}`)
- **Authority / Permission**: Public (🌐 **PUBLIC**)
- **Method**: `GET`
- **Path**: `/api/client/master-data/{id}`
- **Path Parameters**: `id` (UUID string)
- **Response**: `200 OK`
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": {
      "id": "b1000000-0000-0000-0000-000000000002",
      "title": "Sports Games",
      "code": "SPORTS_GAMES",
      "created_on": "2026-11-07T10:00:00",
      "modified_on": "2026-11-07T10:00:00",
      "active": true
    },
    "time": "2026-11-07T10:55:00.123456"
  }
  ```

### 7.2 Get Items by Master Data ID (`GET /api/client/master-data/{id}/items`)
- **Authority / Permission**: Public (🌐 **PUBLIC**)
- **Method**: `GET`
- **Path**: `/api/client/master-data/{id}/items`
- **Path Parameters**: `id` (UUID string)
- **Response**: `200 OK`
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": [
      {
        "id": "c2000000-0000-0000-0000-000000000001",
        "master_data_id": "b1000000-0000-0000-0000-000000000002",
        "title": "Futsal",
        "code": "FUTSAL",
        "description": "5v5 or 6v6 indoor/outdoor futsal",
        "active": true
      }
    ],
    "time": "2026-11-07T10:55:00.123456"
  }
  ```

### 7.3 Get Master Data by Code (`GET /api/client/master-data/code/{code}`)
- **Authority / Permission**: Public (🌐 **PUBLIC**)
- **Method**: `GET`
- **Path**: `/api/client/master-data/code/{code}`
- **Path Parameters**: `code` (string e.g. `CATEGORY`, `SPORTS_GAMES`, `ESPORTS_GAMES`, `TRAVEL_ACTIVITIES`)
- **Response**: `200 OK`
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": [
      {
        "id": "b1000000-0000-0000-0000-000000000002",
        "title": "Sports Games",
        "code": "SPORTS_GAMES",
        "active": true
      }
    ],
    "time": "2026-11-07T10:55:00.123456"
  }
  ```

### 7.4 Get Items directly by Master Data Code (`GET /api/client/master-data/code/{code}/items`)
- **Authority / Permission**: Public (🌐 **PUBLIC**)
- **Method**: `GET`
- **Path**: `/api/client/master-data/code/{code}/items`
- **Path Parameters**: `code` (string e.g. `SPORTS_GAMES`, `ESPORTS_GAMES`, `TRAVEL_ACTIVITIES`, `CATEGORY`)
- **Description**: Directly fetches all active items belonging to the given master data code without needing the UUID.
- **Response**: `200 OK`
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": [
      {
        "id": "c2000000-0000-0000-0000-000000000005",
        "master_data_id": "b1000000-0000-0000-0000-000000000002",
        "title": "Badminton",
        "code": "BADMINTON",
        "description": "Singles and doubles badminton",
        "active": true
      },
      {
        "id": "c2000000-0000-0000-0000-000000000001",
        "master_data_id": "b1000000-0000-0000-0000-000000000002",
        "title": "Futsal",
        "code": "FUTSAL",
        "description": "5v5 or 6v6 indoor/outdoor futsal",
        "active": true
      }
    ],
    "time": "2026-11-07T10:55:00.123456"
  }
  ```

### 7.5 Search Master Data (`POST /api/client/master-data/search`)
- **Authority / Permission**: Public (🌐 **PUBLIC**)
- **Method**: `POST`
- **Path**: `/api/client/master-data/search`
- **Request Body**:
  ```json
  {
    "pageNumber": 1,
    "pageSize": 10,
    "searchParameter": "Sports",
    "active": true
  }
  ```
- **Response**: `200 OK` (Standard `Page<MasterDataResponseDto>` envelope)

---

## 8. Master Data Items (`/api/client/master-data-items`)

### 8.1 Get Master Data Item by ID (`GET /api/client/master-data-items/{id}`)
- **Authority / Permission**: Public (🌐 **PUBLIC**)
- **Method**: `GET`
- **Path**: `/api/client/master-data-items/{id}`
- **Path Parameters**: `id` (UUID string)
- **Response**: `200 OK`
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": {
      "id": "c2000000-0000-0000-0000-000000000001",
      "master_data_id": "b1000000-0000-0000-0000-000000000002",
      "title": "Futsal",
      "code": "FUTSAL",
      "description": "5v5 or 6v6 indoor/outdoor futsal",
      "active": true
    },
    "time": "2026-11-07T10:55:00.123456"
  }
  ```

### 8.2 Search Master Data Items (`POST /api/client/master-data-items/search`)
- **Authority / Permission**: Public (🌐 **PUBLIC**)
- **Method**: `POST`
- **Path**: `/api/client/master-data-items/search`
- **Request Body**:
  ```json
  {
    "pageNumber": 1,
    "pageSize": 10,
    "masterDataId": "b1000000-0000-0000-0000-000000000002",
    "searchParameter": "Futsal",
    "active": true
  }
  ```
- **Response**: `200 OK` (Standard `Page<MasterDataItemResponseDto>` envelope)

---

## 9. Frontend Integration Flow: How FE Retrieves & Uses This Data

When building match creation and filtering interfaces (e.g. Create Match / Challenge Request, Filter Feed):

1. **Option 1: Bootstrap Everything in 1 Call (Recommended for SPAs and Mobile Apps)**:
   - Call `GET /api/client/categories/tree` (or `GET /api/client/categories?includeItems=true`).
   - The backend dynamically queries `system.master_data` and `system.master_data_items` in the database, returning every active category and all its configured games/activities.
   - Store the response in global state (Redux, Pinia, Zustand, React Context).
   - Use `data[i].name` for category tabs/dropdowns (`Sports`, `E-Sports`, `Travel`, or newly created categories).
   - When a category is selected, instantly display its `data[i].items` (`Futsal`, `Valorant`, `Hiking`, etc.) without an extra network call!

2. **Option 2: Dependent Dropdown Dynamic Fetching**:
   - Call `GET /api/client/categories` on mount to populate the Category selector.
   - When user selects `SPORTS`, trigger `GET /api/client/categories/SPORTS/items` (or `GET /api/client/master-data/code/SPORTS_GAMES/items`) to populate the Game selector.

3. **Submitting to Create Match Request (`POST /api/client/match-requests`)**:
   - Send `category`: e.g. `"SPORTS"` (or `"E_SPORTS"`, `"TRAVEL"`, or any custom category code).
   - Send `game`: Selected item's title or code (e.g. `"Futsal"` or `"FUTSAL"`).
   - Send `location`: e.g. `"Dhanyenthar Futsal, Kathmandu"`.
   - Send `players_needed`: integer e.g. `2` (when searching for teammates).
   - Send `time`: ISO 8601 future date/time e.g. `"2026-11-08T18:00:00"`.

4. **Handling Backend Master Data Validation**:
   - The backend validates that:
     1. The `category` exists as an active entry in the master data database. If not, it responds with `400 Bad Request`:
        `{"code": "400", "message": "Invalid category: '...'. Category must be an active category defined in master data."}`
     2. The `game` exists as an active item under that specific `category`. If not, or if a user submits a game from a different category, it responds with `400 Bad Request`:
        `{"code": "400", "message": "Invalid game: '...'. Game must be a valid active game/activity under category '...' from master data."}`
   - The frontend should map these `400` messages directly to UI field errors or toast notifications.


