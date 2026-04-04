# EmployeeHub

A multi-tenant HR management REST API. Users can belong to multiple companies and manage employee profiles, salary history, job title history, and documents within each workspace.

## Tech Stack

- **Java 21** · **Spring Boot 4** · **Spring Security (JWT)** · **Spring Data JPA**
- **PostgreSQL** · **AWS S3** · **Lombok** · **Jakarta Validation**

## Prerequisites

- Java 21
- Maven (or use `./mvnw`)
- Docker (for local Postgres)
- AWS S3 bucket + IAM user with S3 read/write access

## Getting Started

**1. Start Postgres**
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
> Make sure all environment variables from `.env` are available to the process (e.g. via IDE run configuration or exporting them in your shell).

API is available at `http://localhost:8080`.

## Authentication

JWT tokens are stored in HttpOnly cookies. Standard flow: `POST /auth/register` → verify email → `POST /auth/login`. Use `POST /auth/refresh` to rotate the token pair and `POST /auth/logout` to clear cookies.

Password reset is available via `POST /auth/forgot-password` → `POST /auth/reset-password?token=`.

## Roles & Permissions

| Role | Permissions |
|---|---|
| `OWNER` | Full access including company settings |
| `HR` | Manage members, salary, job titles, documents |
| `MANAGER` | View member directory |
| `EMPLOYEE` | View member directory + self-service via `/me` endpoints |

Self-service (`/me`) is enabled per member via `selfServiceEnabled` flag and requires `ACTIVE` status.

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
