# Reimbursement Management Service

Microservice responsible for managing employee travel expense reimbursements in the **Employee Travel Desk (ETD)** system. After a business trip, an employee submits invoices (food, water, laundry, local travel) with PDF proof. A Travel Desk Executive then reviews and approves or rejects each claim.

---

## Table of Contents

1. [Tech Stack](#tech-stack)
2. [Service Overview](#service-overview)
3. [Getting Started](#getting-started)
4. [Configuration](#configuration)
5. [Architecture](#architecture)
6. [Database Schema](#database-schema)
7. [Startup Data Seeding](#startup-data-seeding)
8. [API Reference](#api-reference)
9. [Business Rules & Requirements](#business-rules--requirements)
10. [Authentication & Authorization](#authentication--authorization)
11. [File Upload](#file-upload)
12. [Error Handling](#error-handling)
13. [Inter-service Communication](#inter-service-communication)
14. [Known Constraints & Notes](#known-constraints--notes)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.7 |
| Build tool | Gradle (no wrapper JAR committed — use system `gradle`) |
| Persistence | Spring Data JPA + Hibernate |
| Database | MySQL 8 (own DB `reimbursement_management`) |
| Security | Spring Security 6 + stateless JWT (JJWT 0.12.6, HMAC-SHA256) |
| HTTP clients | Spring Cloud OpenFeign |
| API docs | Springdoc OpenAPI (Swagger UI) |
| Utilities | Lombok |

---

## Service Overview

| Property | Value |
|---|---|
| Port | **8084** |
| Base path | `/api/reimbursements` |
| Spring app name | `reimbursement-management` |
| Package | `com.etd.reimbursement_management` |

### Role in the ETD System

| Service | Port | Responsibility |
|---|---|---|
| auth-service | 8080 | Login, token issuance, logout, blacklist |
| account-management | 8081 | Employee & grade CRUD |
| travel-planner | 8082 | Travel request lifecycle, budget calculation |
| reservation-management | 8083 | Reservation booking & document management |
| **reimbursement-management** | **8084** | **Expense claim submission & TravelDeskExe processing** |

---

## Getting Started

### Prerequisites

- Java 21
- Gradle (system install)
- MySQL 8 running on `localhost:3306` with a `reimbursement_management` database (username `root`)
- account-management running (creates the shared `account_management` MySQL schema + seeds default users)
- auth-service running on port 8080
- travel-planner running on port 8082

### Startup Order

```
1. account-management  → creates shared `account_management` MySQL schema + seeds default users
2. auth-service        → connects to the same `account_management` MySQL DB (TCP 3306)
3. travel-planner      → own MySQL DB `travel_planner`
4. reservation-management → own MySQL DB `reservation_management`
5. reimbursement-management   ← this service (own MySQL DB `reimbursement_management`)
```

### Run

```bash
gradle bootRun
```

### Build

```bash
gradle build
```

### Swagger UI

`http://localhost:8084/swagger-ui.html`

---

## Configuration

All settings in `src/main/resources/application.properties`:

```properties
# Server
server.port=8084
spring.application.name=reimbursement-management

# MySQL Database (own DB: reimbursement_management)
spring.datasource.url=jdbc:mysql://localhost:3306/reimbursement_management
spring.datasource.username=root
spring.datasource.password=<your-password>
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# JWT (must match all other ETD services exactly)
jwt.secret=etdTravelDeskJwtSecretKey1234567890ABCDEF

# Dependent service URLs
auth.service.base_url=http://localhost:8080/
travel.planner.service.base_url=http://localhost:8082/
travel.planner.service.url=api/
account.management.service.base_url=http://localhost:8081/
account.management.service.url=api/

# File upload directory (relative to working dir, auto-created on first upload)
app.upload.dir=./uploads
```

---

## Architecture

### Layer Flow

```
HTTP Request
     ↓
JwtAuthFilter  (validates JWT, checks blacklist via auth-service Feign)
     ↓
SecurityConfig (role-based access enforcement)
     ↓
Controller     (@RestController, @CrossOrigin)
     ↓
Service Interface → Service Impl
     ↓                     ↓
Mapper               Feign Clients (travel-planner, account-management, auth-service)
     ↓
JPA Repository → MySQL
```

### Package Layout

```
com.etd.reimbursement_management
├── client/
│   ├── AuthServiceClient.java          Feign → GET /auth/blacklist/check
│   ├── TravelPlannerClient.java        Feign → GET /api/travelrequests/{trid}
│   └── AccountManagementClient.java    Feign → GET /api/employees/{id}
├── config/
│   ├── DataInitializer.java            Seeds reimbursement types on startup
│   ├── FeignAuthInterceptor.java       Forwards Bearer token to all outgoing Feign calls
│   ├── JwtAuthFilter.java              Per-request JWT validation filter
│   └── SecurityConfig.java             Role-based security rules
├── constant/
│   └── AppConstant.java                All string constants and message keys
├── controller/
│   ├── ReimbursementRequestController.java   POST /add, GET /*, PUT /*/process
│   └── ReimbursementTypeController.java      GET /types
├── dao/
│   ├── ReimbursementRequsetRepo.java   findByTravelRequestId(Long)
│   └── ReimbursementTypeRepo.java      Standard JpaRepository
├── dto/
│   ├── ReimbursementRequestDTO.java    Incoming request body for adding a reimbursement
│   ├── ProcessReimbursementDTO.java    Incoming request body for approve/reject
│   ├── ReimbursementResponseDTO.java   Outgoing response for any reimbursement
│   ├── ReimbursementTypeResponseDTO.java
│   └── ErrorDTO.java
├── entity/
│   ├── ReimbursementRequest.java       Table: reimbursement_requests
│   └── ReimbursementType.java          Table: reimbursement_types
├── exception/
│   ├── BadRequestException.java
│   ├── DocumentSizeLimitExceededException.java
│   ├── IllegalArgumentException.java
│   ├── NotFoundException.java
│   └── GlobalExceptionHandler.java     @RestControllerAdvice
├── mapper/
│   ├── ReimbursementRequestMapper.java
│   └── ReimbursementTypeMapper.java
├── service/
│   ├── interfaces/
│   │   ├── ReimbursementRequestService.java
│   │   └── ReimbursementTypeService.java
│   └── classes/
│       ├── ReimbursementRequestServiceImpl.java
│       └── ReimbursementTypeServiceImpl.java
└── util/
    └── JWTUtil.java                    extractUsername, extractRole, validateToken
```

---

## Database Schema

### `reimbursement_types`

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT (PK, identity) | Auto-generated |
| `type` | VARCHAR | "Food", "Water", "Laundry", "LocalTravel" |

### `reimbursement_requests`

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT (PK, identity) | Auto-generated |
| `travel_request_id` | BIGINT | Travel request ID from travel-planner (no DB FK — cross-service) |
| `request_raised_by_employee_id` | BIGINT | Employee who submitted the claim |
| `request_date` | DATE | Auto-set to current date on submission |
| `reimbursement_type_id` | BIGINT (FK) | References `reimbursement_types.id` |
| `invoice_no` | VARCHAR | Invoice / receipt number |
| `invoice_date` | DATE | Date on the invoice (must be within trip dates) |
| `invoice_amount` | BIGINT | Amount in INR |
| `document_url` | VARCHAR | PDF filename (not full path) |
| `request_processed_on` | DATE | Set when approved/rejected |
| `request_processed_by_employee_id` | BIGINT | TravelDeskExe who processed it |
| `status` | VARCHAR | `"New"` → `"Approved"` or `"Rejected"` |
| `remarks` | VARCHAR | Mandatory when rejecting |

---

## Startup Data Seeding

`DataInitializer` (implements `ApplicationRunner`) runs on every startup. **Idempotent** — seeds only when `reimbursement_types` table is empty.

| Auto-assigned ID | Type Name | Description |
|---|---|---|
| 1 | Food | Meal expenses |
| 2 | Water | Drinking water expenses |
| 3 | Laundry | Laundry service expenses |
| 4 | LocalTravel | Local cab/auto expenses during the trip |

> **Warning:** IDs are assigned by identity sequence in the order seeded. Never manually insert or reorder types — the business logic references them by name string, not by ID, so reordering is safe as long as names match the constants in `AppConstant`.

---

## API Reference

### Authentication

All endpoints except Swagger require a valid JWT:
```
Authorization: Bearer <token>
```
Tokens are issued by **auth-service** (`POST http://localhost:8080/login`).

---

### GET `/api/reimbursements/types`

**Description:** Get all available reimbursement types.

**Auth:** Any authenticated user (HR, Employee, TravelDeskExe)

**Response `200 OK`:**
```json
[
  { "id": 1, "type": "Food" },
  { "id": 2, "type": "Water" },
  { "id": 3, "type": "Laundry" },
  { "id": 4, "type": "LocalTravel" }
]
```

---

### POST `/api/reimbursements/add`

**Description:** Submit a new reimbursement claim with an invoice PDF.

**Auth:** Employee only

**Content-Type:** `multipart/form-data`

| Part | Type | Required | Description |
|---|---|---|---|
| `reimbursementRequestDTO` | JSON (application/json) | Yes | Claim details |
| `pdfFile` | File | Yes | Invoice PDF (max 256 KB, must be PDF) |

**`reimbursementRequestDTO` fields:**

| Field | Type | Required | Description |
|---|---|---|---|
| `travelRequestId` | Long | Yes | ID of the travel request this expense is for |
| `requestRaisedByEmployeeId` | Long | Yes | Must match the employee who raised the travel request |
| `reimbursementTypeId` | Long | Yes | ID from `GET /api/reimbursements/types` |
| `invoiceNo` | String | Yes | Receipt / invoice number |
| `invoiceDate` | Date | Yes | Date on the invoice (must be within trip from–to dates) |
| `invoiceAmount` | Long | Yes | Amount in INR (must be within daily limits) |

**Sample request body (JSON part):**
```json
{
  "travelRequestId": 1,
  "requestRaisedByEmployeeId": 100003,
  "reimbursementTypeId": 1,
  "invoiceNo": "REST-001",
  "invoiceDate": "2025-12-02",
  "invoiceAmount": 1200
}
```

**Response `200 OK`:**
```json
{
  "id": 1,
  "travelRequestId": 1,
  "requestRaisedByEmployeeId": 100003,
  "requestDate": "2025-12-05",
  "reimbursementType": "Food",
  "invoiceNo": "REST-001",
  "invoiceDate": "2025-12-02",
  "invoiceAmount": 1200,
  "documentUrl": "1749300000000_invoice.pdf",
  "requestProcessedOn": null,
  "requestProcessedByEmployeeId": null,
  "status": "New",
  "remarks": null
}
```

**Possible error responses:**

| Status | Reason |
|---|---|
| 400 | PDF content-type is not application/pdf |
| 400 | Invalid reimbursement type ID |
| 400 | Travel request not found / travel-planner unreachable |
| 400 | `requestRaisedByEmployeeId` does not match who raised the travel request |
| 400 | Invoice date is outside the travel request from–to date range |
| 400 | Invoice amount is outside the allowed range for the type |
| 400 | Daily combined budget exceeded for the type on that invoice date |
| 400 | Failed to save PDF file |
| 403 | Not authenticated / wrong role (not Employee) |
| 413 | PDF exceeds 256 KB |

---

### GET `/api/reimbursements/{travelRequestId}/requests`

**Description:** Get all reimbursement claims submitted for a specific travel request.

**Auth:** Any authenticated user (HR, Employee, TravelDeskExe)

**Path variable:** `travelRequestId` (Long)

**Response `200 OK`:** Array of `ReimbursementResponseDTO`

**Response `404`:** No reimbursements found for that travel request ID

---

### GET `/api/reimbursements/{reimbursementId}`

**Description:** Get a single reimbursement claim by its ID.

**Auth:** Any authenticated user (HR, Employee, TravelDeskExe)

**Path variable:** `reimbursementId` (Long)

**Response `200 OK`:** Single `ReimbursementResponseDTO`

**Response `404`:** Reimbursement not found

---

### PUT `/api/reimbursements/{reimbursementId}/process`

**Description:** Approve or reject a reimbursement claim. Only TravelDeskExe can call this.

**Auth:** TravelDeskExe only

**Path variable:** `reimbursementId` (Long)

**Request body (`application/json`):**

| Field | Type | Required | Description |
|---|---|---|---|
| `requestProcessedByEmployeeId` | Long | Yes | Employee ID of the TravelDeskExe processing the request |
| `status` | String | Yes | `"Approved"` or `"Rejected"` (case-sensitive) |
| `remarks` | String | Conditional | Required when `status = "Rejected"` |

**Sample request body:**
```json
{
  "requestProcessedByEmployeeId": 100002,
  "status": "Approved",
  "remarks": ""
}
```

**Response `200 OK`:** Updated `ReimbursementResponseDTO` with `status`, `requestProcessedOn`, `requestProcessedByEmployeeId` filled in.

**Possible error responses:**

| Status | Reason |
|---|---|
| 400 | `status` is not `"Approved"` or `"Rejected"` |
| 400 | `status = "Rejected"` but `remarks` is empty |
| 400 | Request is already approved or rejected |
| 400 | `requestProcessedByEmployeeId` is not a TravelDeskExe |
| 403 | Not authenticated / wrong role (not TravelDeskExe) |
| 404 | Reimbursement not found |

---

## Business Rules & Requirements

All BRs are derived from the ETD project specification (`CDE-EmployeeTravelDesk-V1.0.1.docx`).

---

### BR-1: Only Employee can submit reimbursements

Only users with role `Employee` can call `POST /api/reimbursements/add`. HR and TravelDeskExe cannot submit claims.

---

### BR-2: Only TravelDeskExe can process (approve/reject) reimbursements

Only users with role `TravelDeskExe` can call `PUT /api/reimbursements/{id}/process`. Additionally, the `requestProcessedByEmployeeId` in the request body is validated against account-management to confirm they are actually a TravelDeskExe.

---

### BR-3: Employee ID must match the travel request

The `requestRaisedByEmployeeId` in the reimbursement request must be the same employee who originally raised the travel request in travel-planner. Cross-employee submissions are rejected.

---

### BR-4: Invoice date must fall within the travel period

The `invoiceDate` must be on or between the travel request's `fromDate` and `toDate` (both inclusive). Expenses outside the travel dates are not claimable.

---

### BR-5: PDF invoice is mandatory

Every reimbursement submission requires a PDF document (invoice/receipt) uploaded as a multipart file.

---

### BR-6: PDF size limit — 256 KB

The uploaded PDF must not exceed **262,144 bytes (256 KB)**. Larger files are rejected with HTTP 413.

---

### BR-7: Only PDF files accepted

The uploaded file must have Content-Type `application/pdf`. Non-PDF files are rejected with HTTP 400.

---

### BR-8: Amount range limits per reimbursement type

Each invoice amount must fall within the allowed range for its type:

| Type | Minimum | Maximum |
|---|---|---|
| Food | ₹1,000 | ₹1,500 |
| Water | ₹1,000 | ₹1,500 |
| Laundry | ₹250 | ₹500 |
| LocalTravel | ₹0 | ₹1,000 |

---

### BR-9: Daily cumulative budget limits per type

In addition to per-invoice range limits, the **combined total** for all claims of the same category on the same invoice date cannot exceed:

| Category | Daily Combined Limit |
|---|---|
| Food + Water (combined) | ₹1,500 / day |
| Laundry | ₹500 / day |
| LocalTravel | ₹1,000 / day |

**Example:** If an employee already claimed Food = ₹1,200 on December 2nd, they cannot claim Water on the same date (₹1,200 + any Water amount ≥ ₹1,000 would exceed ₹1,500).

---

### BR-10: Reimbursement cannot be processed twice

Once a request has status `"Approved"` or `"Rejected"`, it cannot be processed again. Any attempt returns an error with the date it was previously processed.

---

### BR-11: Rejection requires remarks

When setting status to `"Rejected"`, the `remarks` field is mandatory and must be non-empty. Approvals do not require remarks.

---

### BR-12: Status values are case-sensitive

Status must be exactly `"Approved"` or `"Rejected"`. Any other value (including `"approved"`, `"APPROVED"`) is rejected with HTTP 400.

---

## Authentication & Authorization

### JWT Structure

Tokens issued by **auth-service** carry:
- `sub` — user's email address
- `role` — `"HR"`, `"Employee"`, or `"TravelDeskExe"`
- `iat` — issued-at timestamp
- `exp` — 1 hour from issuance

Signed with HMAC-SHA256 using:
```
jwt.secret=etdTravelDeskJwtSecretKey1234567890ABCDEF
```

### Token Validation Flow (every request)

```
1. Extract Bearer token from Authorization header
2. Parse username (sub claim) — skip silently if invalid/expired
3. Call auth-service → GET /auth/blacklist/check?token=...
   → blacklisted: do not authenticate (Spring Security returns 403)
   → auth-service unreachable: skip check, validate locally only (fail-open)
4. Validate signature + expiry via JWTUtil.validateToken(token)
5. Extract role claim → set SimpleGrantedAuthority in SecurityContextHolder
```

### Access Matrix

| Endpoint | HR | Employee | TravelDeskExe |
|---|---|---|---|
| `GET /api/reimbursements/types` | ✅ | ✅ | ✅ |
| `POST /api/reimbursements/add` | ❌ | ✅ | ❌ |
| `GET /api/reimbursements/{travelRequestId}/requests` | ✅ | ✅ | ✅ |
| `GET /api/reimbursements/{reimbursementId}` | ✅ | ✅ | ✅ |
| `PUT /api/reimbursements/{reimbursementId}/process` | ❌ | ❌ | ✅ |

---

## File Upload

### Upload

- Endpoint: `POST /api/reimbursements/add` (multipart part named `pdfFile`)
- Max size: **256 KB** (262,144 bytes)
- Accepted MIME type: `application/pdf`
- Storage location: configured via `app.upload.dir` (default: `./uploads`)
- Directory is auto-created on first upload — no manual setup required
- Stored filename format: `{epoch_millis}_{original_filename}` (e.g., `1749300000000_invoice.pdf`)
- Only the **filename** is stored in `document_url` — not the full path
- If PDF save fails, a `BadRequestException` is thrown and the `@Transactional` method rolls back the DB insert

> **Important:** Do not change `app.upload.dir` after data has been written — stored filenames will no longer resolve.

---

## Error Handling

All errors are returned as `ErrorDTO`:

```json
{
  "message": "Human-readable error message",
  "fieldName": "The field that caused the error (may be null)",
  "status": "BAD_REQUEST"
}
```

### HTTP Status Codes

| Code | When |
|---|---|
| 200 | Success |
| 400 | Validation failure, business rule violation, bad status/remarks |
| 403 | Missing/invalid/blacklisted token, or wrong role |
| 404 | Reimbursement or type not found |
| 413 | PDF exceeds 256 KB |

### Exception Classes

| Class | HTTP | Cause |
|---|---|---|
| `BadRequestException` | 400 | Invalid PDF format, failed file save |
| `IllegalArgumentException` | 400 | Invalid type ID, travel request not found, employee ID mismatch, invoice date out of range, amount out of range, daily limit exceeded, invalid status, empty remarks, non-TravelDeskExe processor |
| `NotFoundException` | 404 | Reimbursement not found |
| `DocumentSizeLimitExceededException` | 413 | PDF > 256 KB |

### All Error Messages (`messages.properties`)

| Key | Message |
|---|---|
| `reimbursement.not.found.for.travel.request.id` | No Reimbursement found for travel request id: {0}. |
| `reimbursement.not.found` | Reimbursement not found by this reimbursement id: {0}. |
| `reimbursement.type.not.found` | Reimbursement Type doesn't exist by this id: {0}. |
| `travel.request.not.found` | Travel Request does not exist by this Travel Request id: {0}. |
| `invalid.request.employee.id` | Reimbursement request can be raised by same person who has raised the travel request. |
| `pdf.size.exceed` | PDF size should not exceed 256KB. |
| `invalid.pdf.format` | Only PDF files are allowed. |
| `invalid.invoice.date` | Invoice Date {0} must be between travel request from date {1} and to date {2}. |
| `invalid.food.water.invoice.amount` | Invoice amount for Food or Water should be in between 1000 and 1500. |
| `invalid.laundry.invoice.amount` | Invoice amount for Laundry should be in between 250 and 500. |
| `invalid.local.travel.invoice.amount` | Invoice amount for Local Travel should not exceed by 1000. |
| `exceed.budget.limit.for.reimbursement` | You have exceeded the budget limit for this type of reimbursement for date {0} |
| `invalid.status` | You have provided the wrong status. Status can be Approved or Rejected. |
| `empty.remarks` | You have to provide the remarks if you are rejecting the request. |
| `invalid.travel.desk.exe.id` | Only travel desk executive can process the request. |
| `reimbursement.request.already.processed` | This Reimbursement Request is already processed on {0}. |

---

## Inter-service Communication

### Feign Client: auth-service
```
GET http://localhost:8080/auth/blacklist/check?token={token}
Returns: Boolean
```
Called by `JwtAuthFilter` on every authenticated request to verify the token has not been logged out.

### Feign Client: travel-planner
```
GET http://localhost:8082/api/travelrequests/{travelRequestId}
Returns: ObjectNode (JSON)
```
Called during `addReimbursement`. Fields used from the response:

| Field | Purpose |
|---|---|
| `raisedByEmployeeId` | Matched against `requestRaisedByEmployeeId` to prevent cross-employee claims |
| `fromDate` | Lower bound for invoice date validation |
| `toDate` | Upper bound for invoice date validation |

### Feign Client: account-management
```
GET http://localhost:8081/api/employees/{employeeId}
Returns: ObjectNode (JSON)
```
Called during `processReimbursement`. Field used:

| Field | Purpose |
|---|---|
| `role` | Verified to be `"TravelDeskExe"` before allowing the request to be processed |

### Token Forwarding (`FeignAuthInterceptor`)

All outgoing Feign calls automatically carry the incoming `Authorization: Bearer <token>` header. This is required because travel-planner and account-management also validate JWT on their endpoints.

---

## Known Constraints & Notes

### Cross-service ID references (no foreign keys)

`reimbursement_requests.travel_request_id` and `request_raised_by_employee_id` reference entities in other microservices. There are **no database-level foreign keys** — only application-level validation via Feign calls.

### No UserDetailsService

This service does **not** query the employee database for authentication. The JWT `role` claim is trusted directly after signature verification. This keeps the service stateless and independent.

### `ReimbursementRequsetRepo` — typo in class name

The DAO interface is named `ReimbursementRequsetRepo` (missing the 't' in "Request"). This is a known typo from the original codebase. It is referenced in the service impl and functions correctly despite the name.

### Food + Water share a daily budget

Food and Water are treated as a combined category for the daily ₹1,500 limit. You cannot have Food = ₹1,200 and Water = ₹1,000 on the same day — the combined total (₹2,200) exceeds ₹1,500. In practice this means only one Food or Water claim can be submitted per day.
