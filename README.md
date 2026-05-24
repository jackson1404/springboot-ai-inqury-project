# Spring AI OpenRouter Demo — Security + JPA + React

This project is a scalable learning backend/frontend sample using:

- Spring Boot 3.5.x
- Spring AI with OpenRouter through OpenAI-compatible API config
- PostgreSQL + Spring Data JPA
- Spring Security + JWT authentication
- BCrypt password hashing
- Protected REST endpoints
- PostgreSQL-backed AI tools
- React + Vite frontend in JavaScript

## Architecture

```text
frontend React/Vite
    ↓ Authorization: Bearer JWT
Spring Boot controllers
    ↓
Spring Security JWT filter
    ↓
Services
    ↓
JPA repositories / Spring AI ChatClient
    ↓
PostgreSQL / OpenRouter
```

## Main backend features

### Authentication

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`

Register and login return a JWT access token. Protected requests must send:

```text
Authorization: Bearer <token>
```

Seed users are inserted when `APP_DB_SEED_ENABLED=true`:

```text
jack@example.com / Password123   role ADMIN
demo@example.com / Password123   role USER
```

### Protected APIs

These require JWT:

- `POST /api/chat`
- `GET /api/data/customers`
- `GET /api/data/orders`
- `GET /api/data/products`
- `POST /api/data/customers/search`
- `POST /api/data/orders/search`
- `POST /api/data/products/search`

Public APIs:

- `GET /api/ping`
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /actuator/health`

## Backend setup

### 1. Start PostgreSQL

```bash
docker compose up -d
```

Adminer is available at:

```text
http://localhost:8081
```

Adminer login:

```text
System: PostgreSQL
Server: postgres
Username: postgres
Password: postgres
Database: spring_ai_demo
```

### 2. Configure backend env vars

In IntelliJ Run Configuration, set:

```text
OPENROUTER_API_KEY=your_openrouter_key;OPENROUTER_BASE_URL=https://openrouter.ai/api;OPENROUTER_MODEL=openrouter/free;POSTGRES_HOST=localhost;POSTGRES_PORT=5432;POSTGRES_DB=spring_ai_demo;POSTGRES_USER=postgres;POSTGRES_PASSWORD=postgres;APP_DB_SEED_ENABLED=true;JWT_SECRET=replace-with-a-long-random-secret-at-least-32-characters;CORS_ALLOWED_ORIGINS=http://localhost:5173
```

### 3. Run backend

```bash
mvn spring-boot:run
```

Backend runs at:

```text
http://localhost:8080
```

## Backend test commands

### Health check

```bash
curl http://localhost:8080/api/ping
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"jack@example.com","password":"Password123"}'
```

Copy `accessToken` from the response.

### Protected data API

```bash
curl http://localhost:8080/api/data/customers \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Protected AI chat

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"message":"Find orders for customer CUST-1001 and calculate total spend."}'
```

## Frontend setup

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at:

```text
http://localhost:5173
```

The frontend supports:

- login/register
- JWT storage in localStorage for learning/demo purposes
- protected AI chat
- protected customer/order/product data explorer

For production, prefer secure HTTP-only cookies or a stronger token strategy depending on your deployment and threat model.

## Important production notes

Do not commit real secrets. Set them through deployment environment variables, Docker secrets, AWS Secrets Manager, Kubernetes Secret, or another secret manager.

For production, use:

```text
APP_DB_DDL_AUTO=validate
APP_DB_SHOW_SQL=false
APP_DB_SEED_ENABLED=false
JWT_SECRET=<strong random secret>
CORS_ALLOWED_ORIGINS=https://your-real-frontend-domain.com
```

## Important package purpose

```text
entity/       JPA database table mappings
repository/   Spring Data JPA database query interfaces
service/      business logic
controller/   HTTP API layer
dto/          request/response schemas
security/     JWT, Spring Security, authentication filter, config
tool/         AI-callable backend tools backed by PostgreSQL
frontend/     React Vite UI
```
