# Prescription Management REST API

A production-grade, enterprise medical prescription management RESTful API built with **Spring Boot 3.3.4** and **Java 21 LTS**.

---

## 📋 Table of Contents
1. [Overview](#overview)
2. [Architecture & Design Principles](#architecture--design-principles)
3. [Technology Stack](#technology-stack)
4. [Prerequisites](#prerequisites)
5. [Getting Started & Docker Setup](#getting-started--docker-setup)
6. [Default Credentials](#default-credentials)
7. [API Specification & cURL Examples](#api-specification--curl-examples)
8. [Idempotency & Transaction Semantics](#idempotency--transaction-semantics)
9. [Running Tests](#running-tests)
10. [AI Tool Disclosure](#ai-tool-disclosure)

---

## 🏥 Overview

The **Prescription Management REST API** provides healthcare organizations with a robust, HIPAA/GDPR-aligned backend service for managing:
- **Medical Prescribers (Doctors/Physicians)**: Registered healthcare practitioners with unique license identifiers.
- **Clients (Patients)**: Patient demographic and contact records.
- **Prescriptions & Entries**: Atomic creation and life-cycle management of medical prescriptions and associated medication line items.
- **Role-Based Security**: Role-based access control (RBAC) separating administrative actions from standard clinician operations.

---

## 🏛️ Architecture & Design Principles

The application adheres to clean architecture principles:
- **Domain-Driven Design (DDD)**: Entities model clear domain boundaries with bi-directional relationship helpers (`addEntry`, `removeEntry`) to prevent inconsistent state.
- **Java 21 LTS Features**: Uses Java Records for immutable Data Transfer Objects (DTOs), pattern matching, text blocks, and virtual thread readiness.
- **Jakarta EE 10**: Uses `jakarta.persistence.*` and `jakarta.validation.*` standard namespaces.
- **Stateless JWT Security**: Spring Security 6 filter chain with BCrypt password hashing (strength 12) and method-level security (`@PreAuthorize`).
- **Flyway Database Migrations**: Versioned database schemas (`V1` to `V5`), strictly preventing unmanaged Hibernate schema drift (`validate` mode).
- **Centralized Exception Handling**: `@RestControllerAdvice` translates internal exceptions into uniform `ApiResponse<T>` envelopes without leaking internal stack traces.

---

## 🛠️ Technology Stack

| Layer | Component | Version |
|---|---|---|
| **Language** | Java (OpenJDK) | 21 LTS |
| **Framework** | Spring Boot | 3.3.4 |
| **Persistence** | Spring Data JPA / Hibernate | 6.x |
| **Database** | PostgreSQL | 16 |
| **Database Migrations** | Flyway | 10.x |
| **Security** | Spring Security 6 + JJWT | 0.12.6 |
| **API Documentation** | SpringDoc OpenAPI (Swagger UI) | 2.6.0 |
| **Testing** | JUnit 5, Mockito, MockMvc, AssertJ, H2 / Testcontainers | 1.20.1 |
| **Build Tool** | Apache Maven | 3.9+ |
| **Containerization** | Docker & Docker Compose | 20+ |

---

## ⚙️ Prerequisites

- **Java Development Kit (JDK)**: Java 21 or higher
- **Maven**: 3.9+ (or use `./mvnw`)
- **Docker & Docker Compose**: Optional for containerized database execution

---

## 🚀 Getting Started & Docker Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd prescription-management-api
```

### 2. Start PostgreSQL via Docker Compose
To start PostgreSQL 16 database in the background:
```bash
docker compose up -d
```
The container exposes port `5432` with database `prescription_db`, username `postgres`, password `postgres`.

### 3. Run the Spring Boot Application
```bash
mvn spring-boot:run
```
Upon startup:
- Flyway automatically applies migrations (`V1` through `V5`).
- The application starts on `http://localhost:8080`.
- Swagger UI is accessible at: `http://localhost:8080/swagger-ui.html`.
- OpenAPI specification is accessible at: `http://localhost:8080/v3/api-docs`.

---

## 🔐 Default Credentials

Default seeded user accounts (created by Flyway migration `V5__seed_users.sql`):

| Username | Password | Role | Permissions |
|---|---|---|---|
| `admin` | `Admin@123` | `ROLE_ADMIN` | Full access (CRUD prescribers, update/delete prescriptions, user registration) |
| `user` | `User@123` | `ROLE_USER` | Read all, create clients, create prescriptions, append/remove entries |

---

## 📡 API Specification & cURL Examples

All responses follow the unified envelope:
```json
{
  "success": true,
  "data": { ... },
  "message": "OK",
  "errors": null
}
```

### 1. Authentication

#### Authenticate (Login)
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "Admin@123"}'
```

#### Register User (Admin Only)
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"username": "clinician1", "password": "Password@123", "role": "ROLE_USER"}'
```

---

### 2. Prescribers

#### List Prescribers (Paginated)
```bash
curl -X GET "http://localhost:8080/api/prescribers?page=0&size=10&sort=name,asc" \
  -H "Authorization: Bearer <TOKEN>"
```

#### Create Prescriber (Admin Only)
```bash
curl -X POST http://localhost:8080/api/prescribers \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Dr. Gregory House",
    "registrationNumber": "MED-MD-10023",
    "phoneNumber": "+1-555-0145"
  }'
```

#### Get Prescriber by ID
```bash
curl -X GET http://localhost:8080/api/prescribers/1 \
  -H "Authorization: Bearer <TOKEN>"
```

#### Update Prescriber (Admin Only)
```bash
curl -X PUT http://localhost:8080/api/prescribers/1 \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Dr. Gregory House",
    "registrationNumber": "MED-MD-10023",
    "phoneNumber": "+1-555-9999"
  }'
```

#### Delete Prescriber (Admin Only)
```bash
curl -X DELETE http://localhost:8080/api/prescribers/1 \
  -H "Authorization: Bearer <TOKEN>"
```

---

### 3. Clients (Patients)

#### List Clients (Paginated)
```bash
curl -X GET "http://localhost:8080/api/clients?page=0&size=10" \
  -H "Authorization: Bearer <TOKEN>"
```

#### Create Client
```bash
curl -X POST http://localhost:8080/api/clients \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "James Wilson",
    "dateOfBirth": "1975-04-12",
    "phoneNumber": "+1-555-0298"
  }'
```

#### Get Client by ID
```bash
curl -X GET http://localhost:8080/api/clients/1 \
  -H "Authorization: Bearer <TOKEN>"
```

#### Update Client
```bash
curl -X PUT http://localhost:8080/api/clients/1 \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "James Wilson",
    "dateOfBirth": "1975-04-12",
    "phoneNumber": "+1-555-7777"
  }'
```

---

### 4. Prescriptions & Medication Entries

#### List Prescriptions (Paginated & Sortable)
```bash
curl -X GET "http://localhost:8080/api/prescriptions?page=0&size=10&sort=recordDate,desc" \
  -H "Authorization: Bearer <TOKEN>"
```

#### Create Prescription (Atomic with Entries and Idempotency Key)
```bash
curl -X POST http://localhost:8080/api/prescriptions \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "prescriberId": 1,
    "clientId": 1,
    "recordDate": "2026-10-14",
    "diagnosis": "Type 2 Diabetes Mellitus",
    "idempotencyKey": "a9d7e6c4-550e-41d4-a716-446655440000",
    "entries": [
      {
        "entryName": "Metformin 500mg",
        "quantity": 60.00,
        "frequency": "Twice daily with meals",
        "durationDays": 30,
        "instructions": "Take with breakfast and dinner"
      },
      {
        "entryName": "Lisinopril 10mg",
        "quantity": 30.00,
        "frequency": "Once daily in the morning",
        "durationDays": 30,
        "instructions": "Monitor blood pressure"
      }
    ]
  }'
```

#### Get Prescription by ID
```bash
curl -X GET http://localhost:8080/api/prescriptions/1 \
  -H "Authorization: Bearer <TOKEN>"
```

#### Update Prescription Status / Diagnosis (Admin Only)
```bash
curl -X PUT http://localhost:8080/api/prescriptions/1 \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "diagnosis": "Type 2 Diabetes Mellitus (Under Control)",
    "status": "ACTIVE"
  }'
```

#### Add Medication Entry to Prescription
```bash
curl -X POST http://localhost:8080/api/prescriptions/1/entries \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "entryName": "Atorvastatin 20mg",
    "quantity": 30.00,
    "frequency": "Once daily at bedtime",
    "durationDays": 30,
    "instructions": "Take at night"
  }'
```

#### Remove Medication Entry from Prescription
```bash
curl -X DELETE http://localhost:8080/api/prescriptions/1/entries/1 \
  -H "Authorization: Bearer <TOKEN>"
```

#### Delete Prescription (Admin Only)
```bash
curl -X DELETE http://localhost:8080/api/prescriptions/1 \
  -H "Authorization: Bearer <TOKEN>"
```

---

## 🔒 Idempotency & Transaction Semantics

### 1. Idempotency Key Pattern
- In clinical environments, network retries or transient connection drops must never lead to accidental duplication of medical prescriptions.
- Clients supply a client-generated UUID `idempotencyKey` in the `CreatePrescriptionRequest`.
- When a request is received:
  1. The service queries `PrescriptionRepository.findByIdempotencyKey(key)`.
  2. If a prescription with this key is found, the system skips all inserts and returns the existing prescription record with HTTP 200/201 semantics.
  3. A PostgreSQL `UNIQUE` constraint on the `idempotency_key` column guarantees data integrity at the database level against concurrent race conditions.

### 2. Transaction Boundaries
- All mutating operations in `PrescriptionService` are annotated with `@Transactional`.
- Cascade configuration (`CascadeType.ALL`, `orphanRemoval = true`) ensures that adding or removing prescription line items updates the parent and children entities inside a single physical database transaction.
- If an invalid client ID, prescriber ID, or medication constraint fails, the entire transaction rolls back cleanly, leaving zero orphaned records.

---

## 🧪 Running Tests

The test suite consists of unit tests, controller slice tests, and an end-to-end integration test:

```bash
mvn test
```

### Test Suite Structure
1. **`PrescriptionServiceTest`**: Mockito unit test suite validating business logic, idempotency replay, prescriber validation, and entry modification.
2. **`PrescriptionControllerTest`**: Spring MVC slice test (`@WebMvcTest`) with MockMvc verifying HTTP response codes, input validation, and JSON payload contracts.
3. **`PrescriptionIntegrationTest`**: `@SpringBootTest` testing full end-to-end flow with JWT security, database transactions, cascading persistence, and idempotency across endpoints.

---

## 🤖 AI Tool Disclosure

| Aspect | AI Contribution | Human Oversight & Refinement |
|---|---|---|
| **API Design** | Drafted REST controller contracts and OpenAPI schema mappings | Reviewed REST conventions, parameter validation rules, and status codes |
| **DTOs & Entity Modeling** | Scaffolding Java 21 Record DTOs and JPA entity mappings | Enforced bi-directional consistency helpers and immutability invariants |
| **Security Architecture** | JWT token filter structure and Spring Security 6 integration | Set BCrypt strength to 12 and strict method-level authorization constraints |
| **Idempotency Strategy** | Suggested idempotency key lookup and database unique constraint pattern | Implemented transaction atomicity and idempotent return semantics |
| **Test Scenarios** | Generated test cases for Mockito, MockMvc, and SpringBootTest | Verified edge cases: invalid payloads, missing resources, and cascade deletes |

---

## 📄 License
This project is licensed under the Apache 2.0 License.
