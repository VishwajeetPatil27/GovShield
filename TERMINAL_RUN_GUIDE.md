# GovShield Run Guide (Terminal)

## 1. Go To Project Folder

```powershell
cd "c:\Users\vspat\OneDrive\Desktop - Copy\Desktop\GovShield"
```

## 2. Prerequisites Check

```powershell
java -version
mvn -version
node -v
npm -v
mysql --version
```

## 3. Database Setup

1. Login:

```powershell
mysql -u root -p
```

2. Run in MySQL:

```sql
CREATE DATABASE IF NOT EXISTS govshield;
USE govshield;
SOURCE database/schema.sql;
SOURCE database/data.sql;
SHOW TABLES;
EXIT;
```

## 4. Passwords

1. App users:

- `admin@govshield.gov.in` / `admin@2727`
- `officer1@govshield.gov.in` / `officer@2727`
- `auditor@govshield.gov.in` / `auditor@2727`

2. MySQL:

- Use your root password.
- Recommended app user:

```sql
CREATE USER IF NOT EXISTS 'govshield_app'@'localhost' IDENTIFIED BY 'GovShield@2727';
GRANT ALL PRIVILEGES ON govshield.* TO 'govshield_app'@'localhost';
FLUSH PRIVILEGES;
```

3. Export env vars before backend start:

```powershell
$env:DB_USERNAME="govshield_app"
$env:DB_PASSWORD="GovShield@2727"
```

## 5. Run Manually

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

## 6. Verify

1. API:

```powershell
curl http://localhost:8080/api/schemes/active/all
```

2. Login test:

```powershell
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d "{\"email\":\"admin@govshield.gov.in\",\"password\":\"admin@2727\"}"
```

3. Browser:

- Frontend: `http://localhost:5500/frontend/index.html`
- Login: `http://localhost:5500/frontend/login.html`
- Swagger: `http://localhost:8080/swagger-ui/index.html`
