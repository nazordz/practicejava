--liquibase formatted sql
-- db/changelog/001-create-users.sql

--changeset nazor:001-create-users
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    phone_number VARCHAR(32),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified_at TIMESTAMPTZ,
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ
);

-- Enforce email uniqueness only for non-deleted rows (soft delete friendly)
CREATE UNIQUE INDEX ux_users_email_active ON users (email) WHERE deleted_at IS NULL;
CREATE INDEX ix_users_deleted_at ON users (deleted_at);

--rollback DROP TABLE users;

--changeset nazor:002-create-roles
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    name VARCHAR(64) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
--rollback DROP TABLE roles;

--changeset nazor:003-create-permissions
CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    name VARCHAR(128) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
--rollback DROP TABLE permissions;

--changeset nazor:004-create-role-permissions
CREATE TABLE role_permissions (
    role_id UUID NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions (id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);
CREATE INDEX ix_role_permissions_permission_id ON role_permissions (permission_id);
--rollback DROP TABLE role_permissions;

--changeset nazor:005-create-user-roles
CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);
CREATE INDEX ix_user_roles_role_id ON user_roles (role_id);
--rollback DROP TABLE user_roles;

--changeset nazor:006-seed-roles-and-permissions
INSERT INTO roles (name, description) VALUES
    ('ROLE_ADMIN', 'Full administrative access'),
    ('ROLE_USER', 'Standard authenticated user');

INSERT INTO permissions (name, description) VALUES
    ('user:read',   'Read user records'),
    ('user:write',  'Create and update user records'),
    ('user:delete', 'Delete user records');

-- ROLE_ADMIN gets every permission
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ROLE_ADMIN';

-- ROLE_USER can only read
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name = 'user:read'
WHERE r.name = 'ROLE_USER';
--rollback DELETE FROM role_permissions; DELETE FROM permissions; DELETE FROM roles;
