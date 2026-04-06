# GovShield Run Guide (VS Code)

## 1. Open Project

1. Open VS Code.
2. `File -> Open Folder` and select:

```text
c:\Users\vspat\OneDrive\Desktop - Copy\Desktop\GovShield
```

3. Open VS Code terminal (`Ctrl + ``).

## 2. Prerequisites Check

Run:

```powershell
java -version
mvn -version
node -v
npm -v
mysql --version
```

## 3. Database Setup

1. In terminal:

```powershell
mysql -u root -p
```

2. In MySQL prompt:

```sql
CREATE DATABASE IF NOT EXISTS govshield;
USE govshield;
SOURCE database/schema.sql;
SOURCE database/data.sql;
SHOW TABLES;
EXIT;
```

## 4. Passwords

1. App login credentials:

- `admin@govshield.gov.in` / `admin@2727`
- `officer1@govshield.gov.in` / `officer@2727`
- `auditor@govshield.gov.in` / `auditor@2727`

2. DB credentials:

- Use your MySQL root password.
- Or create app user:

```sql
CREATE USER IF NOT EXISTS 'govshield_app'@'localhost' IDENTIFIED BY 'GovShield@2026';
GRANT ALL PRIVILEGES ON govshield.* TO 'govshield_app'@'localhost';
FLUSH PRIVILEGES;
```

3. Set env vars in terminal before backend run:

```powershell
$env:DB_USERNAME="govshield_app"
$env:DB_PASSWORD="GovShield@2026"
```

## 5. Run Backend (VS Code Terminal 1)

```powershell
cd backend
mvn spring-boot:run
```

Backend URL:

```text
http://localhost:8080
```

## 6. Run Frontend (VS Code Terminal 2)

```powershell
cd frontend
npx http-server -p 5500
```

Frontend URL:

```text
http://localhost:5500/frontend/index.html
```

## 7. Verify

1. Swagger:

```text
http://localhost:8080/swagger-ui/index.html
```

2. Login page:

```text
http://localhost:5500/frontend/login.html
```
