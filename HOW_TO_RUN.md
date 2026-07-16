# GovShield Setup and Run Guide

This project has three important parts:

- `backend/` for the Spring Boot API
- `frontend/` for the static HTML/CSS/JS app
- `database/` for the MySQL schema and seed data

## Prerequisites

- Java 17
- Maven 3.9 or newer
- Node.js 18 or newer
- MySQL 8

## 1. Create the database

Start MySQL, then create the database and import the schema:

```powershell
mysql -u root -p -e "DROP DATABASE IF EXISTS govshield; CREATE DATABASE govshield;"
Get-Content database/schema.sql | mysql -u root -p govshield
Get-Content database/data.sql | mysql -u root -p govshield
```

If you prefer `cmd.exe`, this also works there:

```cmd
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS govshield;"
mysql -u root -p govshield < database\schema.sql
mysql -u root -p govshield < database\data.sql
```

If your MySQL username or password is different, use those values in the commands above.

If you already tried an import and want to reset everything in one step, run:

```powershell
npm run db:reset
```

This script reads `DB_USERNAME` and `DB_PASSWORD` from `.env` first, then falls back to the defaults.

The backend also imports the root `.env` file directly, so the same values are used when you run `mvn spring-boot:run`.

## 2. Configure environment variables

Copy the example file and edit it if needed:

```powershell
Copy-Item .env.example .env
```

The backend reads these values:

- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION`

The frontend uses:

- `GOVSHIELD_API_BASE_URL`

For local-only use, open the site as `http://localhost:5500/login.html`.

## 3. Install Node dependencies

From the project root:

```powershell
npm install
```

This installs the small root tools used to run the frontend server and start both apps together.

## 4. Start the backend

You can run it directly:

```powershell
cd backend
mvn spring-boot:run
```

The backend API runs on `http://localhost:8080`.

If port `8080` is already in use, stop the old backend process or run with a different port, for example:

```powershell
$env:SERVER_PORT=8081
cd backend
mvn spring-boot:run
```

## 5. Start the frontend

In another terminal:

```powershell
npm run frontend
```

The frontend runs on `http://localhost:5500`.

## 6. Start both together

From the project root:

```powershell
npm run dev
```

This starts:

- backend on `8080`
- frontend on `5500`

## 7. Open the app

Use:

```text
http://localhost:5500
```

The frontend already points to the backend API at:

```text
http://localhost:8080/api
```

If you change the backend port, update `GOVSHIELD_API_BASE_URL` in `.env` to match, for example `http://localhost:8081/api`.

If login still fails in the browser, clear site data for `localhost:5500` so an old saved `GOVSHIELD_API_BASE_URL` does not override the default.

## 8. Useful checks

- If login fails, make sure the backend is running and the database is imported.
- If the frontend cannot reach the API, confirm `GOVSHIELD_API_BASE_URL` is set to `http://localhost:8080/api`.
- If MySQL connection fails, confirm `DB_USERNAME` and `DB_PASSWORD` match your local MySQL account.

## 9. Demo logins

Use these exact demo credentials:

- `admin@govshield.gov.in` / `admin@2727`
- `officer1@govshield.gov.in` / `officer@2727`
- `auditor@govshield.gov.in` / `auditor@2727`

If the browser keeps failing, fully retype the password field. The backend log will show the exact password it received, which is the fastest way to catch autofill mistakes.
