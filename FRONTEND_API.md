# Frontend API Contract & Integration Hub

The API documentation is split into two dedicated, focused documents for clarity:

| Portal | Port | Target Audience | Documentation |
| :--- | :--- | :--- | :--- |
| **Client Portal** | `8022` | Store, merchant, customer, player, and mobile apps | [**`FRONTEND_CLIENT_API.md`**](FRONTEND_CLIENT_API.md) |
| **System Portal** | `8021` | Back-office, administrator, and operations dashboard | [**`FRONTEND_SYSTEM_API.md`**](FRONTEND_SYSTEM_API.md) |

---

## Quick Navigation

### 1. Client Portal APIs ([`FRONTEND_CLIENT_API.md`](FRONTEND_CLIENT_API.md))
- **Base URL**: `http://localhost:8022/api/client`
- **Swagger UI**: [`http://localhost:8022/swagger-ui.html`](http://localhost:8022/swagger-ui.html)
- **OpenAPI Schema**: [`http://localhost:8022/v3/api-docs`](http://localhost:8022/v3/api-docs)
- **Features**:
  - Health & Ping (`/health`, `/api/client/ping`)
  - Client Authentication & Sessions (`/api/client/auth/*`)
  - User Self-Management & Profile (`/api/client/users/*`)
  - Match Requests for Players & Teams (`/api/client/match-requests/*`)

### 2. System Portal APIs ([`FRONTEND_SYSTEM_API.md`](FRONTEND_SYSTEM_API.md))
- **Base URL**: `http://localhost:8021/api/system`
- **Swagger UI**: [`http://localhost:8021/swagger-ui.html`](http://localhost:8021/swagger-ui.html)
- **OpenAPI Schema**: [`http://localhost:8021/v3/api-docs`](http://localhost:8021/v3/api-docs)
- **Features**:
  - Health & Ping (`/health`, `/api/system/ping`)
  - Administrative Authentication (`/api/system/auth/*`)
  - System User Management (`/api/system/users/*`)
  - Master Data Configuration (`/api/system/master-data/*`)
  - Master Data Items Hierarchy (`/api/system/master-data-items/*`)
  - System Match Requests Moderation (`/api/system/match-requests/*`)

---

## Shared Global Architecture & Envelopes

### Global Response Envelope (`ResponseTemplate<T>`)
```json
{
  "code": "0",
  "message": "Success",
  "data": { ... },
  "time": "2026-10-30T18:00:00.123456"
}
```

### Global Error Format
```json
{
  "code": "400",
  "message": "Validation or business error message",
  "data": null,
  "time": "2026-10-30T18:00:00.123456"
}
```

### Standard Pagination Envelope (`Page<T>`)
Endpoints ending in `/search` return:
```json
{
  "code": "0",
  "message": "Success",
  "data": {
    "totalRecords": 45,
    "limit": 10,
    "currentPage": 1,
    "data": [ ... ]
  },
  "time": "2026-10-30T18:00:00.123456"
}
```

### Standard Filter Body (`AbstractFilter`)
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

### Authentication Channels
- **HttpOnly Cookies**: `access_token` and `refresh_token`
- **Bearer Header**: `Authorization: Bearer <token>`
- **Frontend Vite Proxy**: `http://localhost:5173` (proxies `/api/system` → port 8021 and `/api/client` → port 8022)
