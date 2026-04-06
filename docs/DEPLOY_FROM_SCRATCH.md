# GovShield: Deploy From Scratch (Netlify + Render + Railway MySQL)

This guide matches your setup:
- Frontend on **Netlify**
- Backend on **Render**
- Database on **Railway MySQL**

If you also need to rotate passwords/keys/URLs, do that first:
- `docs/ROTATE_PASSWORDS_API_KEYS_AND_URLS.md`

---

## 1) Database (Railway MySQL)

### A) Create MySQL on Railway

1) Create a MySQL database in Railway.
2) Copy these values from Railway:
- host
- port
- database name
- username
- password

### B) Import schema + seed data

From your computer (not inside Railway), connect using the Railway credentials and import:
```bash
mysql -h <RAILWAY_HOST> -P <RAILWAY_PORT> -u <RAILWAY_USER> -p <RAILWAY_DB> < database/schema.sql
mysql -h <RAILWAY_HOST> -P <RAILWAY_PORT> -u <RAILWAY_USER> -p <RAILWAY_DB> < database/data.sql
```

> Tip: If you don’t want demo data in production, skip `database/data.sql` and create only the admin/officer/auditor users you want.

---

## 2) Backend (Render)

### A) Create a new Render Web Service

- Root directory: repo root (or point Render to the repo and configure build/start commands)
- Build command (typical Maven):
  - `cd backend && mvn -DskipTests package`
- Start command:
  - `java -jar backend/target/govshield-*.jar`

### B) Add environment variables in Render

Set these in Render → Environment:

- `SPRING_DATASOURCE_URL`
  - Example:
    - `jdbc:mysql://<RAILWAY_HOST>:<RAILWAY_PORT>/<RAILWAY_DB>?useSSL=true&requireSSL=true`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET` (long random string)

Optional (but recommended):
- `SERVER_PORT` (Render sets `PORT`; Spring usually binds automatically if you map it, but keep `SERVER_PORT` only if needed)

### C) Health check

After deploy, verify:
```bash
curl -f https://<YOUR_RENDER_HOST>/api/schemes/active/all
```

---

## 3) Frontend (Netlify)

### A) Deploy the `frontend/` folder

In Netlify:
- Base directory: repo root
- Publish directory: `frontend`

### B) Point the frontend to your Render API

Your frontend uses `frontend/js/runtime-config.js` and falls back to `http://localhost:8080/api`.

For production, simplest is to set a meta tag in the HTML pages you use:
```html
<meta name="govshield-api-base-url" content="https://<YOUR_RENDER_HOST>/api" />
```

Or set it for quick testing in the browser:
```js
localStorage.setItem("GOVSHIELD_API_BASE_URL", "https://<YOUR_RENDER_HOST>/api");
location.reload();
```

### C) CORS

Because Netlify and Render are different domains, the backend must allow requests from your Netlify site.

If you see CORS errors in the browser console, tell me your Netlify domain and I’ll update the backend CORS config properly (safe origins instead of `*`).

---

## 4) Post-deploy checklist

- [ ] `JWT_SECRET` set to a strong value
- [ ] DB user is not MySQL root
- [ ] Demo credentials rotated/removed (see rotation guide)
- [ ] Frontend API base URL points to Render
- [ ] CORS configured for Netlify domain
