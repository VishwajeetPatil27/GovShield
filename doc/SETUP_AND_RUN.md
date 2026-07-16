# Setup and Run

## Prerequisites

- Java 17
- Maven
- Node.js
- MySQL 8

## Database Setup

```powershell
npm run db:reset
```

If you want to do it manually:

```powershell
mysql -u root -p -e "DROP DATABASE IF EXISTS govshield; CREATE DATABASE govshield;"
Get-Content database/schema.sql | mysql -u root -p govshield
Get-Content database/data.sql | mysql -u root -p govshield
```

## Backend

```powershell
cd backend
mvn spring-boot:run
```

Backend health check:

```text
http://localhost:8080/api/health
```

## Frontend

From the project root:

```powershell
npm run frontend
```

Open:

```text
http://localhost:5500/login.html
```

## Demo Logins

- `admin@govshield.gov.in` / `admin@2727`
- `officer1@govshield.gov.in` / `officer@2727`
- `auditor@govshield.gov.in` / `auditor@2727`

