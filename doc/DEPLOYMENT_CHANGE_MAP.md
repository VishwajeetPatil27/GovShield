# Deployment Change Map

Use this file as your deployment checklist when you move GovShield to GitHub, Render, Railway, and Vercel.

## What you usually deploy

- GitHub: push the repo as-is.
- Frontend: Render static site or Vercel.
- Database: Railway MySQL.
- Backend: Render Web Service is the safest fit for this Spring Boot app.

## Exact lines to change

### 1) Frontend API base URL

Update these files so the frontend points to your deployed backend instead of `localhost`.

- [frontend/js/runtime-config.js](/C:/Users/vspat/OneDrive/Desktop%20-%20Copy/Desktop/GovShield/frontend/js/runtime-config.js)
  - Current line: `const DEFAULT_API_BASE_URL = "http://localhost:8080/api";`
  - Change to your production backend URL, for example:
    ```js
    const DEFAULT_API_BASE_URL = "https://your-backend.onrender.com/api";
    ```

- [frontend/js/auth.js](/C:/Users/vspat/OneDrive/Desktop%20-%20Copy/Desktop/GovShield/frontend/js/auth.js)
  - Current line: `: 'http://localhost:8080/api';`
  - Replace with your deployed backend URL if you keep the fallback:
    ```js
    : 'https://your-backend.onrender.com/api';
    ```

- [frontend/js/chatbot.js](/C:/Users/vspat/OneDrive/Desktop%20-%20Copy/Desktop/GovShield/frontend/js/chatbot.js)
  - Current line: `: 'http://localhost:8080/api';`
  - Replace with your deployed backend URL:
    ```js
    : 'https://your-backend.onrender.com/api';
    ```

- [frontend/citizen-onboard.html](/C:/Users/vspat/OneDrive/Desktop%20-%20Copy/Desktop/GovShield/frontend/citizen-onboard.html)
  - Current line: `: 'http://localhost:8080/api';`
  - Replace with your deployed backend URL:
    ```js
    : 'https://your-backend.onrender.com/api';
    ```

## 2) Backend database and port

Update these files so Spring Boot works with Railway MySQL and Render port assignment.

- [backend/src/main/resources/application.properties](/C:/Users/vspat/OneDrive/Desktop%20-%20Copy/Desktop/GovShield/backend/src/main/resources/application.properties)
  - Current line:
    ```properties
    spring.datasource.url=jdbc:mysql://localhost:3306/govshield?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&zeroDateTimeBehavior=CONVERT_TO_NULL
    ```
  - Replace with Railway MySQL env vars, for example:
    ```properties
    spring.datasource.url=${SPRING_DATASOURCE_URL}
    ```
  - Current lines:
    ```properties
    spring.datasource.username=${DB_USERNAME:root}
    spring.datasource.password=${DB_PASSWORD:root}
    ```
  - Replace with Railway/Render secrets, for example:
    ```properties
    spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
    spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
    ```
  - Current line:
    ```properties
    server.port=${SERVER_PORT:8080}
    ```
  - Replace with Render port support:
    ```properties
    server.port=${PORT:8080}
    ```

## 3) Backend CORS

If you want to lock the API down after deployment, update this file.

- [backend/src/main/java/com/govshield/config/SecurityConfig.java](/C:/Users/vspat/OneDrive/Desktop%20-%20Copy/Desktop/GovShield/backend/src/main/java/com/govshield/config/SecurityConfig.java)
  - Current line:
    ```java
    configuration.setAllowedOrigins(Arrays.asList("*"));
    ```
  - Replace `*` with your deployed frontend URL(s), for example:
    ```java
    configuration.setAllowedOrigins(Arrays.asList(
        "https://your-frontend.onrender.com",
        "https://your-frontend.vercel.app"
    ));
    ```

## 4) Environment file

Update your environment examples so deployment values are obvious.

- [.env.example](/C:/Users/vspat/OneDrive/Desktop%20-%20Copy/Desktop/GovShield/.env.example)
  - Current values are local defaults.
  - Recommended production-style values:
    ```env
    DB_USERNAME=your_railway_user
    DB_PASSWORD=your_railway_password
    JWT_SECRET=use-a-long-random-secret
    JWT_EXPIRATION=86400000
    GOVSHIELD_API_BASE_URL=https://your-backend.onrender.com/api
    ```

## 5) What usually does not need code changes

- [package.json](/C:/Users/vspat/OneDrive/Desktop%20-%20Copy/Desktop/GovShield/package.json): no code change needed unless you want new deploy scripts.
- GitHub repository settings: no code change needed.
- Database SQL files: only change if your seed data must be different.

## Recommended order

1. Deploy Railway MySQL.
2. Deploy backend on Render with the Railway connection string.
3. Deploy frontend on Render or Vercel with the backend API URL.
4. Push everything to GitHub.

## Quick reminder

The backend should read the real database URL and real port from environment variables in production. Do not leave `localhost` in the deployed build.
