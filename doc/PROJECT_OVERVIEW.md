# GovShield Project Overview

GovShield is a unified beneficiary and corruption monitoring platform built to support:

- citizen onboarding and identity management
- scheme eligibility and application processing
- fraud detection and audit tracking
- public project monitoring and transparency

## Main Modules

- `backend/`: Spring Boot REST API, authentication, business rules, and database access
- `frontend/`: static HTML, CSS, and JavaScript dashboards
- `database/`: MySQL schema and sample seed data

## Core Features

- employee and citizen login
- citizen onboarding and UGID generation
- scheme registration and eligibility checks
- fraud risk and conflict rule handling
- project tracking and public alerts
- audit logging and transparency views

## Technology Stack

- Java 17
- Spring Boot 3
- Spring Security
- Spring Data JPA
- MySQL
- HTML, CSS, and vanilla JavaScript
- Node.js tooling for local frontend serving

## Runtime Flow

1. MySQL starts and the `govshield` database is created.
2. The backend loads `backend/src/main/resources/application.properties`.
3. The backend API runs on `http://localhost:8080`.
4. The frontend is served from `http://localhost:5500`.
5. The frontend calls backend endpoints under `http://localhost:8080/api`.

