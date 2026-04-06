# GovShield: Rotate Passwords, API Keys, and Links (URLs)

This guide shows what to change in this repo when you want to:
- change database host/user/password
- change demo/user passwords
- change JWT secret (token signing key)
- change the frontend API base URL (local → hosted)

> Don’t commit real secrets to git. Keep them in your hosting platform environment variables.

---

## 1) Database (Railway MySQL) connection for the backend

GovShield backend reads DB credentials from environment variables.

Set these in **Render** (Backend service → Environment):
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

Example `SPRING_DATASOURCE_URL`:
```text
jdbc:mysql://<RAILWAY_HOST>:<RAILWAY_PORT>/<RAILWAY_DB>?useSSL=true&requireSSL=true
```

Local dev fallback is in `backend/src/main/resources/application.properties`.

---

## 2) JWT secret (token signing key)

Set this in **Render**:
- `JWT_SECRET` = long random string

What happens when you rotate it:
- all existing tokens become invalid (everyone must login again)

Generate a strong secret (one option):
```bash
openssl rand -base64 48
```

---

## 3) Change Admin/Officer/Auditor passwords

GovShield stores employee passwords as **BCrypt hashes** in `gov_employees.password_hash`.

### A) Production (Railway DB already running)

1) Generate a BCrypt hash using the helper:

Windows (PowerShell):
```powershell
cd backend
mvn -DskipTests package
mvn -DskipTests dependency:copy-dependencies -DincludeScope=runtime
java -cp "target/classes;target/dependency/*" com.govshield.PasswordHashGenerator "NEW_PASSWORD_HERE"
```

Linux/macOS:
```bash
cd backend
mvn -DskipTests package
mvn -DskipTests dependency:copy-dependencies -DincludeScope=runtime
java -cp "target/classes:target/dependency/*" com.govshield.PasswordHashGenerator "NEW_PASSWORD_HERE"
```

2) Update the user(s) in MySQL (Railway):
```sql
UPDATE gov_employees
SET password_hash = 'PASTE_BCRYPT_HASH_HERE'
WHERE email IN ('admin@govshield.gov.in','officer1@govshield.gov.in','auditor@govshield.gov.in');
```

### B) If you re-import seed data

Demo hashes are seeded in `database/data.sql`. Update them there before importing.

Also update demo text in:
- `README.md`
- `frontend/index.html`
- `frontend/login.html`
- `docs/api-docs.md`

---

## 4) Change frontend API base URL (Netlify)

Frontend now supports a single runtime config source: `frontend/js/runtime-config.js`.

### Quick test (no redeploy)
Open the site, then in browser console:
```js
localStorage.setItem("GOVSHIELD_API_BASE_URL","https://<YOUR_RENDER_HOST>/api");
location.reload();
```

### Recommended (for production)
Add a meta tag to the HTML pages you deploy:
```html
<meta name="govshield-api-base-url" content="https://<YOUR_RENDER_HOST>/api" />
```

---

## 5) Links checklist (local → hosted)

Update these docs so they don’t show `localhost`:
- `docs/api-docs.md` (Base URL section)
- `README.md` (URLs / credentials)

