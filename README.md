# JWT + Redis Session Management System

A Spring Boot backend that implements **JWT-based authentication** combined with **Redis-backed session management**, enabling real-time session revocation — something plain JWT alone cannot do.

---

## 🚀 Problem Statement

Pure JWT authentication is stateless — once a token is issued, the server cannot invalidate it before its expiry. This means:
- A user who logs out can still use their old token until it naturally expires.
- There's no way to force logout from a compromised session.

This project solves that by pairing JWT with **Redis as a session store**, giving the best of both worlds: fast, stateless token verification *and* the ability to revoke access instantly.

---

## This is how it will work

<img width="1536" height="1024" alt="ChatGPT Image Jul 6, 2026, 03_22_08 PM" src="https://github.com/user-attachments/assets/d801ff22-e508-4f1a-89cc-ffa2c053dee6" />


## 🛠️ Tech Stack

- **Java 17 / Spring Boot**
- **Spring Data JPA** — MySQL persistence
- **MySQL** — user data storage
- **Redis** — session storage with TTL
- **JJWT (io.jsonwebtoken)** — JWT generation & validation
- **Spring Security (BCrypt)** — password hashing
- **Swagger / springdoc-openapi** — API testing & documentation
- **Lombok** — boilerplate reduction

---

## 📐 Architecture / Flow

### 1. Signup
```
Client → POST /api/auth/signup → Password hashed (BCrypt) → Saved in MySQL
```

### 2. Login
```
Client → POST /api/auth/login
  → Verify email + password against MySQL (BCrypt match)
  → Generate JWT token
  → Save session in Redis → key: "session:{email}", value: token, TTL: 1 hour
  → Return JWT to client
```

### 3. Accessing a Protected Route
```
Client → GET /api/auth/profile (Authorization: Bearer <token>)
  → Validate JWT signature & expiry
  → Extract email from token
  → Check Redis for "session:{email}"
      → EXISTS → fetch user from MySQL → return data
      → MISSING → reject with "Session expired or logged out"
```

### 4. Logout
```
Client → POST /api/auth/logout?email=user@test.com
  → Delete "session:{email}" from Redis
  → Old JWT becomes unusable immediately, even though it hasn't technically expired
```

---

## 📡 API Endpoints

| Method | Endpoint              | Description                          | Auth Required |
|--------|------------------------|---------------------------------------|----------------|
| POST   | `/api/auth/signup`    | Register a new user                  | No             |
| POST   | `/api/auth/login`     | Login and receive JWT                | No             |
| GET    | `/api/auth/profile`   | Get logged-in user's profile         | Yes (JWT)      |
| POST   | `/api/auth/logout`    | Invalidate session (Redis)           | No             |

---

## ⚙️ Setup & Run

1. Clone the repo
   ```bash
   git clone <repo-url>
   ```

2. Create MySQL database
   ```sql
   CREATE DATABASE session_db;
   ```

3. Update `application.properties` with your MySQL credentials and Redis host/port.

4. Run Redis locally (default port `6379`).

5. Run the app:
   ```bash
   mvn spring-boot:run
   ```

6. Open Swagger UI:
   ```
   http://localhost:8080/swagger-ui.html
   ```

---

## 🧪 Example Test Flow

```bash
# 1. Signup
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"name":"Aman","email":"aman@test.com","age":22,"password":"123456"}'

# 2. Login (returns JWT)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"aman@test.com","password":"123456"}'

# 3. Access protected profile
curl -X GET http://localhost:8080/api/auth/profile \
  -H "Authorization: Bearer <token>"

# 4. Logout
curl -X POST "http://localhost:8080/api/auth/logout?email=aman@test.com"

# 5. Try profile again with the SAME token -> should now fail
curl -X GET http://localhost:8080/api/auth/profile \
  -H "Authorization: Bearer <token>"
```

---

## 📚 What I Learned Building This

- **Why stateless JWT isn't enough on its own** — and how pairing it with a fast in-memory store like Redis solves real-world problems like logout and session revocation.
- **Cache-aside style session validation** — checking Redis before trusting a token, instead of relying purely on token expiry.
- **BCrypt password hashing** — never storing plain-text passwords, and using `matches()` to verify without ever decrypting.
- **TTL-based expiry in Redis** — letting Redis auto-clean stale sessions instead of manually tracking expiry.
- **JWT internals** — header/payload/signature structure, signing with `HMAC-SHA256`, and how tampering is detected via signature mismatch.
- **Constructor injection with Lombok (`@RequiredArgsConstructor`)** — cleaner, more testable dependency wiring vs. field injection.
- **Spring Security config for stateless APIs** — disabling CSRF and default form login, since JWT handles auth instead of sessions/cookies.
- **Debugging real Spring errors** — e.g. `ObjectOptimisticLockingFailureException` caused by sending an `id` in a create request, and `MissingRequestHeaderException` from a Swagger UI quirk not committing header input.
- **API testing beyond Postman** — integrating Swagger for quicker in-browser testing, and using raw `curl` when Swagger UI didn't behave as expected.

---

## 🔮 Future Improvements

- Refresh token flow (short-lived access token + long-lived refresh token)
- Custom exception classes with proper HTTP status codes (401/403 instead of generic 500)
- "Logout from all devices" using a `userId` key prefix pattern
- Rate limiting on login attempts
- Spring Security filter chain to auto-validate JWT on every request (instead of manual checks per endpoint)

---

## 👤 Author

**Aman** — Backend developer focused on Java/Spring Boot, distributed systems concepts (Redis, Kafka), and building production-style backend systems from scratch.
