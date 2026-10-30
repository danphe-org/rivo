CREATE SCHEMA IF NOT EXISTS system;
CREATE SCHEMA IF NOT EXISTS client;

CREATE TABLE IF NOT EXISTS system.system_users (
    id               UUID PRIMARY KEY,
    name             VARCHAR(100) NOT NULL,
    email            VARCHAR(255) NOT NULL UNIQUE,
    password         VARCHAR(255),
    phone            VARCHAR(30),
    created_by       VARCHAR(36),
    created_on       TIMESTAMP WITH TIME ZONE NOT NULL,
    last_modified_by VARCHAR(36),
    last_modified_on TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_by       VARCHAR(36),
    deleted_on       TIMESTAMP WITH TIME ZONE,
    active           BOOLEAN NOT NULL DEFAULT TRUE,
    deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    remark           TEXT,
    version          BIGINT NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS ix_system_users_email ON system.system_users(email);

CREATE TABLE IF NOT EXISTS system.user_roles (
    id               UUID PRIMARY KEY,
    user_id          UUID NOT NULL REFERENCES system.system_users(id) ON DELETE CASCADE,
    role             VARCHAR(50) NOT NULL DEFAULT 'SYSTEM_USER',
    created_by       VARCHAR(36),
    created_on       TIMESTAMP WITH TIME ZONE NOT NULL,
    last_modified_by VARCHAR(36),
    last_modified_on TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_by       VARCHAR(36),
    deleted_on       TIMESTAMP WITH TIME ZONE,
    active           BOOLEAN NOT NULL DEFAULT TRUE,
    deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    remark           TEXT,
    version          BIGINT NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS ix_system_user_roles_user_id ON system.user_roles(user_id);
CREATE INDEX IF NOT EXISTS ix_system_user_roles_role ON system.user_roles(role);

CREATE TABLE IF NOT EXISTS client.client_users (
    id               UUID PRIMARY KEY,
    name             VARCHAR(100) NOT NULL,
    email            VARCHAR(255) NOT NULL UNIQUE,
    password         VARCHAR(255),
    phone            VARCHAR(30),
    created_by       VARCHAR(36),
    created_on       TIMESTAMP WITH TIME ZONE NOT NULL,
    last_modified_by VARCHAR(36),
    last_modified_on TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_by       VARCHAR(36),
    deleted_on       TIMESTAMP WITH TIME ZONE,
    active           BOOLEAN NOT NULL DEFAULT TRUE,
    deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    remark           TEXT,
    version          BIGINT NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS ix_client_users_email ON client.client_users(email);

CREATE TABLE IF NOT EXISTS client.user_roles (
    id               UUID PRIMARY KEY,
    user_id          UUID NOT NULL REFERENCES client.client_users(id) ON DELETE CASCADE,
    role             VARCHAR(50) NOT NULL DEFAULT 'CLIENT',
    created_by       VARCHAR(36),
    created_on       TIMESTAMP WITH TIME ZONE NOT NULL,
    last_modified_by VARCHAR(36),
    last_modified_on TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_by       VARCHAR(36),
    deleted_on       TIMESTAMP WITH TIME ZONE,
    active           BOOLEAN NOT NULL DEFAULT TRUE,
    deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    remark           TEXT,
    version          BIGINT NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS ix_client_user_roles_user_id ON client.user_roles(user_id);
CREATE INDEX IF NOT EXISTS ix_client_user_roles_role ON client.user_roles(role);

CREATE TABLE IF NOT EXISTS client.match_requests (
    id               UUID PRIMARY KEY,
    requested_by     UUID NOT NULL REFERENCES client.client_users(id) ON DELETE CASCADE,
    request_type     VARCHAR(50) NOT NULL,
    status           VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    game             VARCHAR(100) NOT NULL,
    category         VARCHAR(100),
    message          TEXT,
    time             TIMESTAMP,
    urgent           BOOLEAN NOT NULL DEFAULT FALSE,
    created_by       VARCHAR(36),
    created_on       TIMESTAMP WITH TIME ZONE NOT NULL,
    last_modified_by VARCHAR(36),
    last_modified_on TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_by       VARCHAR(36),
    deleted_on       TIMESTAMP WITH TIME ZONE,
    active           BOOLEAN NOT NULL DEFAULT TRUE,
    deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    remark           TEXT,
    version          BIGINT NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS ix_match_requests_requested_by ON client.match_requests(requested_by);
CREATE INDEX IF NOT EXISTS ix_match_requests_request_type ON client.match_requests(request_type);
CREATE INDEX IF NOT EXISTS ix_match_requests_status ON client.match_requests(status);
CREATE INDEX IF NOT EXISTS ix_match_requests_game ON client.match_requests(game);
CREATE INDEX IF NOT EXISTS ix_match_requests_created_on ON client.match_requests(created_on DESC);

CREATE TABLE IF NOT EXISTS system.master_data (
    id               UUID PRIMARY KEY,
    title            VARCHAR(255) NOT NULL,
    code             VARCHAR(100) NOT NULL,
    created_by       VARCHAR(36),
    created_on       TIMESTAMP WITH TIME ZONE NOT NULL,
    last_modified_by VARCHAR(36),
    last_modified_on TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_by       VARCHAR(36),
    deleted_on       TIMESTAMP WITH TIME ZONE,
    active           BOOLEAN NOT NULL DEFAULT TRUE,
    deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    remark           TEXT,
    version          BIGINT NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS ix_master_data_code ON system.master_data(code);
CREATE INDEX IF NOT EXISTS ix_master_data_title ON system.master_data(title);
CREATE INDEX IF NOT EXISTS ix_master_data_created_on ON system.master_data(created_on DESC);

CREATE TABLE IF NOT EXISTS system.master_data_items (
    id               UUID PRIMARY KEY,
    master_data_id   UUID NOT NULL,
    title            VARCHAR(255) NOT NULL,
    code             VARCHAR(100),
    description      TEXT,
    created_by       VARCHAR(36),
    created_on       TIMESTAMP WITH TIME ZONE NOT NULL,
    last_modified_by VARCHAR(36),
    last_modified_on TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_by       VARCHAR(36),
    deleted_on       TIMESTAMP WITH TIME ZONE,
    active           BOOLEAN NOT NULL DEFAULT TRUE,
    deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    remark           TEXT,
    version          BIGINT NOT NULL DEFAULT 1,
    CONSTRAINT fk_master_data_items_master_data
        FOREIGN KEY (master_data_id)
        REFERENCES system.master_data(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_master_data_items_master_data_id ON system.master_data_items(master_data_id);
CREATE INDEX IF NOT EXISTS ix_master_data_items_code ON system.master_data_items(code);
CREATE INDEX IF NOT EXISTS ix_master_data_items_title ON system.master_data_items(title);
CREATE INDEX IF NOT EXISTS ix_master_data_items_created_on ON system.master_data_items(created_on DESC);
CREATE UNIQUE INDEX IF NOT EXISTS ux_master_data_items_parent_title
    ON system.master_data_items (master_data_id, LOWER(title))
    WHERE deleted = FALSE;

CREATE TABLE IF NOT EXISTS system.audit_logs (
    id           VARCHAR(36) PRIMARY KEY,
    entity_key   VARCHAR(255),
    request_type VARCHAR(50),
    new_data     JSONB,
    old_data     JSONB,
    requested_by VARCHAR(255),
    requested_on TIMESTAMP,
    responded_by VARCHAR(255),
    responded_on TIMESTAMP,
    remarks      TEXT,
    entity       VARCHAR(100),
    resource     VARCHAR(255)
);

CREATE OR REPLACE VIEW public.users AS
    SELECT id, name, email, password, phone, created_by, created_on, last_modified_by, last_modified_on, deleted_by, deleted_on, active, deleted, remark, version FROM system.system_users
    UNION ALL
    SELECT id, name, email, password, phone, created_by, created_on, last_modified_by, last_modified_on, deleted_by, deleted_on, active, deleted, remark, version FROM client.client_users;

CREATE OR REPLACE VIEW public.user_roles AS
    SELECT id, user_id, role, created_by, created_on, last_modified_by, last_modified_on, deleted_by, deleted_on, active, deleted, remark, version FROM system.user_roles
    UNION ALL
    SELECT id, user_id, role, created_by, created_on, last_modified_by, last_modified_on, deleted_by, deleted_on, active, deleted, remark, version FROM client.user_roles;

CREATE OR REPLACE VIEW public.match_requests AS SELECT * FROM client.match_requests;
CREATE OR REPLACE VIEW public.master_data AS SELECT * FROM system.master_data;
CREATE OR REPLACE VIEW public.master_data_items AS SELECT * FROM system.master_data_items;
CREATE OR REPLACE VIEW public.audit_logs AS SELECT * FROM system.audit_logs;
CREATE OR REPLACE VIEW system.users AS SELECT * FROM system.system_users;
CREATE OR REPLACE VIEW client.users AS SELECT * FROM client.client_users;

INSERT INTO system.system_users (
    id, name, email, password, phone,
    created_by, created_on, last_modified_by, last_modified_on,
    active, deleted, remark, version
) VALUES (
    'e300e50e-144f-4787-aed7-1c8bc2690dd1',
    'Super Admin',
    'superadmin@bazar.com',
    '$2a$12$UT8i.iyMaO.XJVq5lXazbOuoMGJPLxzQ/JpI4m/fowOR0lMmuqfWy',
    '+9779800000000',
    'SYSTEM',
    NOW(),
    'SYSTEM',
    NOW(),
    true,
    false,
    'Default seeded system administrator (bazar)',
    1
) ON CONFLICT (id) DO UPDATE SET
    email = EXCLUDED.email,
    password = EXCLUDED.password,
    active = true,
    deleted = false;

INSERT INTO system.user_roles (
    id, user_id, role,
    created_by, created_on, last_modified_by, last_modified_on,
    active, deleted, remark, version
) VALUES (
    'a1b2c3d4-0000-0000-0000-000000000001',
    'e300e50e-144f-4787-aed7-1c8bc2690dd1',
    'SYSTEM_USER',
    'SYSTEM',
    NOW(),
    'SYSTEM',
    NOW(),
    true,
    false,
    'Default system administrator role',
    1
) ON CONFLICT (id) DO UPDATE SET
    role = EXCLUDED.role,
    active = true,
    deleted = false;

INSERT INTO system.system_users (
    id, name, email, password, phone,
    created_by, created_on, last_modified_by, last_modified_on,
    active, deleted, remark, version
) VALUES (
    'e300e50e-144f-4787-aed7-1c8bc2690dd2',
    'Super Admin',
    'superadmin@rivo.com',
    '$2a$12$UT8i.iyMaO.XJVq5lXazbOuoMGJPLxzQ/JpI4m/fowOR0lMmuqfWy',
    '+9779800000001',
    'SYSTEM',
    NOW(),
    'SYSTEM',
    NOW(),
    true,
    false,
    'Default seeded system administrator (rivo)',
    1
) ON CONFLICT (id) DO UPDATE SET
    email = EXCLUDED.email,
    password = EXCLUDED.password,
    active = true,
    deleted = false;

INSERT INTO system.user_roles (
    id, user_id, role,
    created_by, created_on, last_modified_by, last_modified_on,
    active, deleted, remark, version
) VALUES (
    'a1b2c3d4-0000-0000-0000-000000000002',
    'e300e50e-144f-4787-aed7-1c8bc2690dd2',
    'SYSTEM_USER',
    'SYSTEM',
    NOW(),
    'SYSTEM',
    NOW(),
    true,
    false,
    'Default system administrator role',
    1
) ON CONFLICT (id) DO UPDATE SET
    role = EXCLUDED.role,
    active = true,
    deleted = false;
