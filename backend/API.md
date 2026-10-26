# Frontend API Contract & Integration Guide

This document is the single source of truth for Frontend (FE) web applications communicating with the backend services. It specifies the base envelopes, authentication flow, error formatting, and exact Request/Response payloads for all API endpoints, mirroring the standards of the `danphe/bazar_backend` project.

> [!IMPORTANT]
> **RULE BOOK REQUIREMENT**: Every time a new CRUD operation, endpoint, request parameter, or response field is added or modified, this file **MUST** be updated immediately in the same commit/change.

---

## 1. General Architecture & Envelopes

### Base URLs & Portals
- **System Portal API (Back-office / Admin)**: `http://localhost:8000/api/system`
- **Client Portal API (Store / Merchant / Customer)**: `http://localhost:8000/api/client`
- **Versioned API Root**: `http://localhost:8000/api/v1`
- **Interactive Swagger Documentation**: [`http://localhost:8000/docs`](http://localhost:8000/docs)
- **ReDoc Documentation**: [`http://localhost:8000/redoc`](http://localhost:8000/redoc)
- **OpenAPI JSON Schema**: [`http://localhost:8000/openapi.json`](http://localhost:8000/openapi.json)

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
  * Allowed origins in development: `http://localhost:3000`, `http://localhost:5173`, `http://localhost:8000`.

---

## 2. System Portal APIs (`/api/system`)

Used exclusively by the back-office / admin portal.

### 2.1 Health Check (`/api/system/ping`)
- **Authority / Permission**: None (🟢 **PUBLIC**)
- **Method**: `GET`
- **Path**: `/api/system/ping`
- **Request Parameters / Body**: None
- **Response**: `200 OK`
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": "pong",
    "time": "2026-10-22T09:30:00.123456"
  }
  ```

---

## 3. Client Portal APIs (`/api/client`)

Used by the client / store / customer portal.

### 3.1 Health Check (`/api/client/ping`)
- **Authority / Permission**: None (🟢 **PUBLIC**)
- **Method**: `GET`
- **Path**: `/api/client/ping`
- **Request Parameters / Body**: None
- **Response**: `200 OK`
  ```json
  {
    "code": "0",
    "message": "Success",
    "data": "pong",
    "time": "2026-10-22T09:30:00.123456"
  }
  ```

---

## 4. System Utility & Infrastructure APIs

### 4.1 Orchestrator Health Check (`/health`)
- **Authority / Permission**: None (🟢 **PUBLIC**)
- **Method**: `GET`
- **Path**: `/health`
- **Description**: Lightweight health probe for Docker / Kubernetes load balancers.
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

---

## 5. Frontend Integration Quickstart

### 5.1 TypeScript Interface Definitions

```typescript
// types/api.ts

export interface ResponseTemplate<T> {
  code: string;       // "0" = success, others = error
  message: string;    // Human-readable message
  data: T;            // Response payload
  time: string;       // ISO timestamp
}

export interface Page<T> {
  totalRecords: number;
  limit: number;
  currentPage: number;
  data: T[];
}

export interface AbstractFilter {
  pageNumber?: number;
  pageSize?: number;
  searchParameter?: string;
  sortField?: string;
  sortOrder?: "ASC" | "DESC";
  active?: boolean;
}
```

### 5.2 Axios Client Configuration with Credentials

```typescript
// api/client.ts
import axios, { AxiosResponse } from "axios";
import type { ResponseTemplate } from "../types/api";

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:8000",
  timeout: 10000,
  withCredentials: true, // Required for HttpOnly auth cookies matching bazar-backend
  headers: {
    "Content-Type": "application/json",
    Accept: "application/json",
  },
});

// Response Interceptor: Unwrap envelope & validate success code
apiClient.interceptors.response.use(
  (response: AxiosResponse<ResponseTemplate<any>>) => {
    if (response.data && response.data.code !== "0") {
      return Promise.reject(new Error(response.data.message || "Request failed"));
    }
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      window.location.href = "/login";
    }
    const message = error.response?.data?.message || error.message || "Network Error";
    return Promise.reject(new Error(message));
  }
);
```

### 5.3 Invoking the Ping Endpoints

```typescript
// api/ping.ts
import { apiClient } from "./client";
import type { ResponseTemplate } from "../types/api";

// System Back-Office Ping
export async function pingSystem(): Promise<string> {
  const res = await apiClient.get<ResponseTemplate<string>>("/api/system/ping");
  return res.data.data; // "pong"
}

// Client Store Portal Ping
export async function pingClient(): Promise<string> {
  const res = await apiClient.get<ResponseTemplate<string>>("/api/client/ping");
  return res.data.data; // "pong"
}
```
