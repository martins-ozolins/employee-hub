CREATE TABLE companies (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    industry    VARCHAR(100),
    location    VARCHAR(255),
    description VARCHAR(1000),
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ
);

CREATE TABLE users (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email           VARCHAR(255) NOT NULL UNIQUE,
  password_hash   TEXT NOT NULL,
  first_name      VARCHAR(100) NOT NULL,
  last_name       VARCHAR(100) NOT NULL,
  middle_name     VARCHAR(100),
  is_active       BOOLEAN NOT NULL,
  email_verified  BOOLEAN NOT NULL DEFAULT false,
  role            VARCHAR(50) NOT NULL,
  created_at      TIMESTAMPTZ NOT NULL,
  updated_at      TIMESTAMPTZ
);

CREATE TABLE company_members (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID REFERENCES users(id) ON DELETE SET NULL,
    company_id              UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    role                    VARCHAR(50) NOT NULL,
    self_service_enabled    BOOLEAN NOT NULL,
    employment_status       VARCHAR(50) NOT NULL,
    first_name              VARCHAR(100) NOT NULL,
    last_name               VARCHAR(100) NOT NULL,
    middle_name             VARCHAR(100),
    work_email              VARCHAR(255),
    personal_email          VARCHAR(255),
    phone_number            VARCHAR(100),
    job_title               VARCHAR(255),
    current_salary_amount   NUMERIC(15, 2),
    current_salary_currency VARCHAR(3),
    department              VARCHAR(255),
    join_date               DATE,
    date_of_birth           DATE,
    address                 VARCHAR(500),
    personal_code           VARCHAR(100),
    bank_account            VARCHAR(100),
    emergency_contact_name  VARCHAR(255),
    emergency_contact_phone VARCHAR(50),
    created_at              TIMESTAMPTZ NOT NULL,
    updated_at              TIMESTAMPTZ,

    CONSTRAINT uk_user_company  UNIQUE (user_id, company_id),
    CONSTRAINT uk_email_company UNIQUE (personal_email, company_id)
);

CREATE TABLE refresh_tokens (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    jti        UUID NOT NULL UNIQUE,
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked    BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ
);

CREATE TABLE verification_tokens (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token      UUID NOT NULL UNIQUE,
    type       VARCHAR(50) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at    TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE salary_history (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_member_id UUID NOT NULL REFERENCES company_members(id) ON DELETE CASCADE,
    amount            NUMERIC(15, 2) NOT NULL,
    currency          VARCHAR(3) NOT NULL,
    effective_date    DATE NOT NULL,
    notes             VARCHAR(500),
    created_at        TIMESTAMPTZ NOT NULL
);

CREATE TABLE job_title_history (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_member_id UUID NOT NULL REFERENCES company_members(id) ON DELETE CASCADE,
    job_title         VARCHAR(200) NOT NULL,
    change_type       VARCHAR(20) NOT NULL,
    effective_date    DATE NOT NULL,
    notes             VARCHAR(500),
    created_at        TIMESTAMPTZ NOT NULL
);

CREATE TABLE documents (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_member_id UUID NOT NULL REFERENCES company_members(id) ON DELETE CASCADE,
    file_name         VARCHAR(255) NOT NULL,
    s3_key            TEXT NOT NULL UNIQUE,
    content_type      VARCHAR(100) NOT NULL,
    file_size         BIGINT NOT NULL,
    expiry_date       DATE,
    uploaded_at       TIMESTAMPTZ NOT NULL
);