# EmployeeHub

A multi-tenant HR management REST API. Users can belong to multiple companies and manage employee profiles, salary history, job title history, and documents within each workspace.

## Tech Stack

- **Java 21** · **Spring Boot 4** · **Spring Security (JWT)** · **Spring Data JPA**
- **PostgreSQL** · **Flyway** · **RabbitMQ** · **AWS S3** · **Lombok** · **Jakarta Validation**

## Prerequisites

- Java 21
- Maven (or use `./mvnw`)
- Docker (for local Postgres + RabbitMQ)
- AWS S3 bucket + IAM user with S3 read/write access

## Getting Started

**1. Start dependencies**
```bash
docker-compose up -d
```

**2. Set environment variables**

```bash
cp .env.example .env
```

Fill in the values in `.env`.

**3. Run the app**
```bash
./mvnw spring-boot:run
```
> Make sure all environment variables from `.env` are available to the process.

API is available at `http://localhost:8080`.

## Authentication

JWT tokens are stored in HttpOnly cookies.

1. `POST /auth/register` — create account
2. `GET /auth/verify-email?token=` — verify email (required before login)
3. `POST /auth/login` — receive JWT cookies
4. `POST /auth/refresh` — rotate token pair
5. `POST /auth/logout` — clear cookies

Password reset: `POST /auth/forgot-password` → `POST /auth/reset-password?token=`

## Roles & Permissions

Roles are DB-backed and scoped per company. Each company starts with a system **Owner** role (all permissions, non-deletable). Additional custom roles can be created via the roles API.

| Permission | Owner | HR | Manager | Employee |
|---|:---:|:---:|:---:|:---:|
| `MANAGE_COMPANY` | ✓ | | | |
| `VIEW_MEMBERS` | ✓ | ✓ | ✓ | ✓ |
| `VIEW_MEMBER_DETAILS` | ✓ | ✓ | | |
| `MANAGE_MEMBERS` | ✓ | ✓ | | |
| `MANAGE_SALARY` | ✓ | ✓ | | |
| `MANAGE_JOB_TITLES` | ✓ | ✓ | | |
| `MANAGE_DOCUMENTS` | ✓ | ✓ | | |

Self-service (`/me`) is a separate access path — enabled per member via `selfServiceEnabled` flag, requires `ACTIVE` status, and is independent of the permission system.

## API Overview

| Group | Base path |
|---|---|
| Auth | `/auth` |
| Companies | `/companies` |
| Members | `/companies/{id}/members` |
| Salary history | `/companies/{id}/members/{memberId}/salary` |
| Job title history | `/companies/{id}/members/{memberId}/job-title` |
| Documents | `/companies/{id}/members/{memberId}/documents` |
| Self-service | `/companies/{id}/me` |
| Roles | `/companies/{id}/roles` |
| Permissions catalog | `/permissions` |
