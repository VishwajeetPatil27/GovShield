# GovShield Detailed Run Guide (VS Code + Terminal)

Use the split guides:
- `VS_CODE_RUN_GUIDE.md`
- `TERMINAL_RUN_GUIDE.md`

## 1. Prerequisites

1. Install Java 17 and confirm:
```powershell
java -version
```
2. Install Maven and confirm:
```powershell
mvn -version
```
3. Install Node.js (LTS) and confirm:
```powershell
node -v
npm -v
```
4. Install MySQL 8 and confirm:
```powershell
mysql --version
```

## 2. Database Setup (Detailed)

1. Open terminal in project root:
```powershell
cd "c:\Users\vspat\OneDrive\Desktop - Copy\Desktop\GovShield"
```
2. Login to MySQL:
```powershell
mysql -u root -p
```
3. Run:
```sql
CREATE DATABASE IF NOT EXISTS govshield;
USE govshield;
SOURCE database/schema.sql;
SOURCE database/data.sql;
SHOW TABLES;
EXIT;
```

## 3. Which Passwords To Use

1. App login users from `database/data.sql`:
- `admin@govshield.gov.in` / `admin@2727`
- `officer1@govshield.gov.in` / `officer@2727`
- `auditor@govshield.gov.in` / `auditor@2727`

2. MySQL password:
- Use the root password you set during MySQL installation.
- If unknown, reset MySQL root password first, then continue.

3. Recommended DB user for daily use (instead of root):
```sql
CREATE USER IF NOT EXISTS 'govshield_app'@'localhost' IDENTIFIED BY 'GovShield@2727';
GRANT ALL PRIVILEGES ON govshield.* TO 'govshield_app'@'localhost';
FLUSH PRIVILEGES;
```

Then set environment variables in terminal before backend run:
```powershell
$env:DB_USERNAME="govshield_app"
$env:DB_PASSWORD="GovShield@2727"
```

## 4. Run From VS Code

1. Open folder `GovShield` in VS Code.
2. Open terminal in VS Code.
3. Start backend:
```powershell
cd backend
mvn spring-boot:run
```
4. Open a second terminal and start frontend:
```powershell
cd frontend
npx http-server -p 5500
```
5. Open browser:
- Frontend: `http://localhost:5500/frontend/index.html`
- Login page: `http://localhost:5500/frontend/login.html`
- API docs: `http://localhost:8080/swagger-ui/index.html`

## 5. Run From Terminal (No VS Code)

Run the backend and frontend in two terminals:

Terminal 1:
```powershell
cd backend
mvn spring-boot:run
```

Terminal 2:
```powershell
cd frontend
npx http-server -p 5500
```

## 6. Verify Everything

1. Backend health check:
```powershell
curl http://localhost:8080/api/schemes/active/all
```
2. Login API test:
```powershell
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d "{\"email\":\"admin@govshield.gov.in\",\"password\":\"admin@2727\"}"
```
3. Frontend login test:
- Open `http://localhost:5500/frontend/login.html`
- Login with admin credentials above.

## 7. Common Fixes

1. `Access denied for user`:
- Verify `DB_USERNAME` and `DB_PASSWORD` in same terminal where backend starts.

2. Port already in use:
- Stop old Java/Node processes or change frontend port command to another port.

3. Frontend shows API errors:
- Confirm backend is running first.
- Confirm API base URL in `frontend/js/auth.js` is `http://localhost:8080/api`.
