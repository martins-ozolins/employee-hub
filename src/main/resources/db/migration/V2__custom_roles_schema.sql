-- 1. Permissions catalog (platform-managed, seeded)
CREATE TABLE permissions (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(100) NOT NULL UNIQUE,
    description   VARCHAR(255),
    display_order INTEGER NOT NULL DEFAULT 0
);

INSERT INTO permissions (id, name, description, display_order) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'MANAGE_COMPANY',      'Update/delete company, view full company details', 1),
    ('a0000000-0000-0000-0000-000000000002', 'VIEW_MEMBERS',        'See member list (directory-level)',                 2),
    ('a0000000-0000-0000-0000-000000000003', 'VIEW_MEMBER_DETAILS', 'See full member profile, salary/job title history', 3),
    ('a0000000-0000-0000-0000-000000000004', 'MANAGE_MEMBERS',      'Create/update/delete members',                     4),
    ('a0000000-0000-0000-0000-000000000005', 'MANAGE_SALARY',       'Add salary records',                               5),
    ('a0000000-0000-0000-0000-000000000006', 'MANAGE_JOB_TITLES',   'Add job title records',                            6),
    ('a0000000-0000-0000-0000-000000000007', 'MANAGE_DOCUMENTS',    'Upload/manage documents for any member',            7);

-- 2. Company roles (per-company, supports custom roles)
CREATE TABLE company_roles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id  UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    name        VARCHAR(100) NOT NULL,
    is_system   BOOLEAN NOT NULL DEFAULT false,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ,
    CONSTRAINT uk_company_role_name UNIQUE (company_id, name)
);

CREATE INDEX idx_company_roles_company_id ON company_roles(company_id);

-- 3. Role-permission join table
CREATE TABLE company_role_permissions (
    role_id       UUID NOT NULL REFERENCES company_roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- 4. Replace old role enum column with FK to company_roles
ALTER TABLE company_members DROP COLUMN role;
ALTER TABLE company_members ADD COLUMN role_id UUID NOT NULL REFERENCES company_roles(id);
CREATE INDEX idx_company_members_role_id ON company_members(role_id);
