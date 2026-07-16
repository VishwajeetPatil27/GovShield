# GovShield Detailed Project Overview

## 1. What GovShield Is

GovShield is a government governance and monitoring platform built to centralize citizen onboarding, scheme eligibility, fraud detection, audit logging, and public project tracking in one system.

The goal of the project is to reduce misuse of welfare benefits, improve transparency in public programs, and give different user roles a clean workflow for reviewing, approving, and monitoring records.

## 2. Core Problem It Solves

Government welfare and monitoring systems often struggle with:

- fragmented citizen records
- duplicate or ineligible scheme applications
- weak audit trails
- limited visibility for officers and auditors
- manual tracking of projects and public complaints

GovShield addresses these issues by combining a structured MySQL database, a Spring Boot API, and browser-based dashboards for citizens and officials.

## 3. Main Goals

- manage citizen and employee identities
- generate and use a unique GovShield ID, or UGID
- register and review scheme applications
- detect fraud and suspicious patterns
- track public projects and alerts
- preserve action history in audit logs
- support role-based access for admin, officer, auditor, and citizen users

## 4. High-Level Architecture

GovShield is organized into three layers:

### Frontend

The frontend is a static HTML/CSS/JavaScript application that renders:

- login screen
- citizen onboarding flow
- citizen dashboard
- officer dashboard
- audit dashboard

It calls the backend API through `fetch()` and uses a shared runtime config file for API base URL setup.

### Backend

The backend is a Spring Boot REST API that provides:

- authentication
- citizen CRUD and onboarding
- scheme management
- eligibility checks
- fraud detection
- project monitoring
- document verification
- audit log retrieval

### Database

The database is MySQL. It stores:

- government employees
- citizens
- schemes
- scheme conflict rules
- enrollments
- citizen economic profiles
- citizen documents
- projects
- project updates
- project evidence
- project alerts
- audit logs

## 5. Technology Stack

- Java 17
- Spring Boot 3.3.x
- Spring Security
- Spring Data JPA
- Hibernate ORM
- MySQL
- HTML, CSS, JavaScript
- Node.js for local frontend tooling

## 6. Backend Structure

The backend code is in `backend/src/main/java/com/govshield/`.

### Key packages

- `config/`
  - security, JWT, and Swagger configuration
- `controller/`
  - REST endpoints for auth, citizens, schemes, projects, audits, and eligibility
- `dto/`
  - request and response objects used by APIs
- `exception/`
  - custom exception handling and error responses
- `model/`
  - JPA entity classes mapped to MySQL tables
- `repository/`
  - Spring Data repositories
- `service/`
  - business logic and validation rules
- `util/`
  - helper logic for eligibility, risk scoring, role checks, and UGID generation

### Important backend entry points

- `backend/src/main/java/com/govshield/GovShieldApplication.java`
  - Spring Boot startup class
- `backend/src/main/java/com/govshield/service/AuthService.java`
  - employee and citizen login, token generation, token validation
- `backend/src/main/java/com/govshield/config/SecurityConfig.java`
  - CORS and request access rules

## 7. Frontend Structure

The frontend code is in `frontend/`.

### Main pages

- `index.html`
  - landing page and demo credentials
- `login.html`
  - employee and citizen login
- `citizen-onboard.html`
  - citizen registration and document upload
- `citizen-dashboard.html`
  - citizen workflow views
- `officer-dashboard.html`
  - officer operations and review tools
- `audit-dashboard.html`
  - audit visibility and reporting

### Shared frontend scripts

- `frontend/js/runtime-config.js`
  - resolves the API base URL
- `frontend/js/auth.js`
  - login, logout, session storage, and generic API calls
- `frontend/js/chatbot.js`
  - simple assistant widget with help text and live summary calls

### Shared runtime behavior

The frontend calls the backend at:

```text
http://localhost:8080/api
```

The login page posts JSON to:

```text
http://localhost:8080/api/auth/login
```

## 8. Database Structure

The database scripts live in `database/`.

### Schema file

- `database/schema.sql`

Creates tables, indexes, and views used by the application.

### Data file

- `database/data.sql`

Loads sample employees, citizens, schemes, enrollments, projects, alerts, and audit entries.

### Main entities

- `gov_employees`
  - stores admin, officer, and auditor accounts
- `citizens`
  - stores citizen identity and profile data
- `schemes`
  - stores scheme definitions and eligibility constraints
- `enrollments`
  - stores citizen applications for schemes
- `projects`
  - stores public project records and progress
- `audit_logs`
  - stores history of actions and accountability records

## 9. Security and Authentication

GovShield uses role-based authentication with JWT tokens.

### Employee login

Employees log in using:

- email
- password

### Citizen login

Citizens log in using:

- Aadhaar
- UGID

### Token handling

After login, the backend returns a JWT token that the frontend stores locally and sends on future API requests.

### Security notes

- CORS is enabled for browser access during development.
- CSRF is disabled for the stateless API workflow.
- The backend exposes a health endpoint and auth endpoints without requiring a browser session.

## 10. AI Fraud Detection

GovShield now includes a dedicated fraud scoring module inside the backend.

### Risk inputs

The fraud score is calculated from four simple signals:

- duplicate Aadhaar
- duplicate bank account
- income mismatch with scheme rules
- multiple scheme conflicts

### Backend pieces

- `com.govshield.service.FraudService`
  - calculates the score and stores the latest fraud flag
- `com.govshield.model.FraudFlag`
  - persists the current fraud analysis for a citizen
- `com.govshield.controller.FraudDetectionController`
  - exposes `GET /api/fraud/{citizenId}`

### Risk output

The response returns:

- citizen identity
- risk score
- risk level
- boolean flags for each risk rule
- human-readable reasons

### Database additions

- `bank_account_number` column on `citizens`
- `fraud_flags` table for stored fraud analysis

## 11. Data Flow

### Login flow

1. User opens the login page.
2. Frontend collects credentials.
3. Frontend sends JSON to the auth API.
4. Backend validates the user against MySQL.
5. Backend returns a JWT token and profile details.
6. Frontend stores the session data and redirects to the proper dashboard.

### Citizen onboarding flow

1. Citizen submits onboarding form.
2. Frontend sends citizen details and uploads documents.
3. Backend generates or confirms UGID.
4. Backend stores the citizen record and related documents.

### Scheme application flow

1. Citizen opens the scheme list.
2. Frontend requests eligibility and scheme data.
3. Backend validates rules and conflict checks.
4. Enrollment is stored if the citizen is eligible.

### Monitoring flow

1. Officer or auditor opens dashboards.
2. Frontend loads project, fraud, audit, and eligibility data.
3. Backend returns the current state from MySQL.

## 12. Runtime Setup

### Start database

Import the schema and sample data using:

```powershell
npm run db:reset
```

### Start backend

```powershell
cd backend
mvn spring-boot:run
```

Backend health check:

```text
http://localhost:8080/api/health
```

### Start frontend

From the project root:

```powershell
npm run frontend
```

Open:

```text
http://localhost:5500/login.html
```

### Demo logins

- `admin@govshield.gov.in` / `admin@2727`
- `officer1@govshield.gov.in` / `officer@2727`
- `auditor@govshield.gov.in` / `auditor@2727`

## 13. Useful Endpoints

- `GET /api/health`
  - service health check
- `POST /api/auth/login`
  - employee login
- `POST /api/auth/citizen-login`
  - citizen login
- `GET /api/citizens`
  - citizen list
- `GET /api/schemes`
  - scheme list
- `GET /api/eligibility/citizen/{citizenId}`
  - citizen eligibility records
- `GET /api/projects`
  - project monitoring data
- `GET /api/audit`
  - audit log data

## 14. Important Files

- `backend/src/main/resources/application.properties`
  - database and server configuration
- `.env`
  - local credentials and frontend API URL
- `package.json`
  - root scripts for frontend, backend, and database reset
- `scripts/reset-db.ps1`
  - Windows helper to recreate and import the MySQL database

## 15. Operational Notes

- The project is designed to run locally on `localhost`.
- The backend listens on port `8080` by default.
- The frontend runs on port `5500`.
- If the browser keeps an old API base URL, clear site storage for `localhost:5500`.
- If login fails, check the backend log and verify the exact password being submitted.

## 16. Summary

GovShield is a complete governance monitoring system that combines:

- a secure Spring Boot backend
- a static frontend dashboard experience
- a structured MySQL schema with sample data
- role-based access
- fraud, eligibility, project, and audit workflows

It is intended as a demo-ready platform for welfare monitoring and public accountability.
