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
    version          BIGINT NOT NULL DEFAULT 1,
    verified         BOOLEAN NOT NULL DEFAULT TRUE
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
    version          BIGINT NOT NULL DEFAULT 1,
    verified         BOOLEAN NOT NULL DEFAULT FALSE
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

CREATE TABLE IF NOT EXISTS client.client_otps (
    id                  UUID PRIMARY KEY,
    identifier          VARCHAR(255) NOT NULL,
    otp_hash            VARCHAR(255) NOT NULL,
    purpose             VARCHAR(50) NOT NULL DEFAULT 'REGISTRATION',
    attempts            INT NOT NULL DEFAULT 0,
    max_attempts        INT NOT NULL DEFAULT 5,
    resend_available_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    consumed            BOOLEAN NOT NULL DEFAULT FALSE,
    consumed_at         TIMESTAMP WITH TIME ZONE,
    created_by          VARCHAR(36),
    created_on          TIMESTAMP WITH TIME ZONE NOT NULL,
    last_modified_by    VARCHAR(36),
    last_modified_on    TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_by          VARCHAR(36),
    deleted_on          TIMESTAMP WITH TIME ZONE,
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE,
    remark              TEXT,
    version             BIGINT NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS ix_client_otps_identifier ON client.client_otps(identifier);
CREATE INDEX IF NOT EXISTS ix_client_otps_purpose ON client.client_otps(purpose);
CREATE INDEX IF NOT EXISTS ix_client_otps_expires_at ON client.client_otps(expires_at);

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
    version          BIGINT NOT NULL DEFAULT 1,
    location         VARCHAR(255),
    players_needed   INT
);

CREATE INDEX IF NOT EXISTS ix_match_requests_requested_by ON client.match_requests(requested_by);
CREATE INDEX IF NOT EXISTS ix_match_requests_request_type ON client.match_requests(request_type);
CREATE INDEX IF NOT EXISTS ix_match_requests_status ON client.match_requests(status);
CREATE INDEX IF NOT EXISTS ix_match_requests_game ON client.match_requests(game);
CREATE INDEX IF NOT EXISTS ix_match_requests_category ON client.match_requests(category);
CREATE INDEX IF NOT EXISTS ix_match_requests_location ON client.match_requests(location);
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
    SELECT id, name, email, password, phone, created_by, created_on, last_modified_by, last_modified_on,
           deleted_by, deleted_on, active, deleted, remark, version, verified FROM system.system_users
    UNION ALL
    SELECT id, name, email, password, phone, created_by, created_on, last_modified_by, last_modified_on,
           deleted_by, deleted_on, active, deleted, remark, version, verified FROM client.client_users;

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

WITH seed (id, title, code, remark) AS (
    VALUES
    ('b1000000-0000-0000-0000-000000000001'::uuid, 'Category', 'CATEGORY', 'Predefined activity and match categories'),
    ('b1000000-0000-0000-0000-000000000002'::uuid, 'Sports Games', 'SPORTS_GAMES', 'Predefined physical sports games'),
    ('b1000000-0000-0000-0000-000000000003'::uuid, 'E-Sports Games', 'ESPORTS_GAMES', 'Predefined e-sports video games'),
    ('b1000000-0000-0000-0000-000000000004'::uuid, 'Travel Activities', 'TRAVEL_ACTIVITIES', 'Predefined travel and outdoor activities')
)
INSERT INTO system.master_data (
    id, title, code, created_by, created_on, last_modified_by, last_modified_on, active, deleted, remark, version
)
SELECT seed.id, seed.title, seed.code, 'SYSTEM', NOW(), 'SYSTEM', NOW(), true, false, seed.remark, 1
FROM seed
WHERE NOT EXISTS (
    SELECT 1 FROM system.master_data existing WHERE UPPER(existing.code) = seed.code
)
ON CONFLICT DO NOTHING;

WITH seed (id, parent_code, title, code, description, remark) AS (
    VALUES
    ('c1000000-0000-0000-0000-000000000001'::uuid, 'CATEGORY', 'E-Sports', 'E_SPORTS', 'Electronic sports and competitive video gaming', 'Predefined Category item'),
    ('c1000000-0000-0000-0000-000000000002'::uuid, 'CATEGORY', 'Sports', 'SPORTS', 'Physical outdoor and indoor sports', 'Predefined Category item'),
    ('c1000000-0000-0000-0000-000000000003'::uuid, 'CATEGORY', 'Travel', 'TRAVEL', 'Travel, outdoor adventures, and tours', 'Predefined Category item'),
    ('c2000000-0000-0000-0000-000000000001'::uuid, 'SPORTS_GAMES', 'Futsal', 'FUTSAL', '5v5 or 6v6 indoor/outdoor futsal', 'Predefined Sports item'),
    ('c2000000-0000-0000-0000-000000000002'::uuid, 'SPORTS_GAMES', 'Football', 'FOOTBALL', 'Standard 11v11 association football', 'Predefined Sports item'),
    ('c2000000-0000-0000-0000-000000000003'::uuid, 'SPORTS_GAMES', 'Basketball', 'BASKETBALL', 'Full-court or 3v3 basketball', 'Predefined Sports item'),
    ('c2000000-0000-0000-0000-000000000004'::uuid, 'SPORTS_GAMES', 'Cricket', 'CRICKET', 'Box cricket, T20, and outdoor cricket', 'Predefined Sports item'),
    ('c2000000-0000-0000-0000-000000000005'::uuid, 'SPORTS_GAMES', 'Badminton', 'BADMINTON', 'Singles and doubles badminton', 'Predefined Sports item'),
    ('c3000000-0000-0000-0000-000000000001'::uuid, 'ESPORTS_GAMES', 'Valorant', 'VALORANT', '5v5 tactical character-based shooter', 'Predefined E-Sports item'),
    ('c3000000-0000-0000-0000-000000000002'::uuid, 'ESPORTS_GAMES', 'Counter-Strike 2', 'CS2', 'Competitive FPS shooter', 'Predefined E-Sports item'),
    ('c3000000-0000-0000-0000-000000000003'::uuid, 'ESPORTS_GAMES', 'Dota 2', 'DOTA2', 'MOBA team strategy game', 'Predefined E-Sports item'),
    ('c3000000-0000-0000-0000-000000000004'::uuid, 'ESPORTS_GAMES', 'PUBG Mobile', 'PUBG_MOBILE', 'Battle royale survival shooter', 'Predefined E-Sports item'),
    ('c3000000-0000-0000-0000-000000000005'::uuid, 'ESPORTS_GAMES', 'Mobile Legends', 'MLBB', '5v5 mobile MOBA', 'Predefined E-Sports item'),
    ('c4000000-0000-0000-0000-000000000001'::uuid, 'TRAVEL_ACTIVITIES', 'Hiking', 'HIKING', 'Day hikes and hill trails', 'Predefined Travel item'),
    ('c4000000-0000-0000-0000-000000000002'::uuid, 'TRAVEL_ACTIVITIES', 'Trekking', 'TREKKING', 'Multi-day mountain and trail expeditions', 'Predefined Travel item'),
    ('c4000000-0000-0000-0000-000000000003'::uuid, 'TRAVEL_ACTIVITIES', 'Camping', 'CAMPING', 'Outdoor camping and night stargazing', 'Predefined Travel item'),
    ('c4000000-0000-0000-0000-000000000004'::uuid, 'TRAVEL_ACTIVITIES', 'Cycling', 'CYCLING', 'Road and mountain biking trips', 'Predefined Travel item')
)
INSERT INTO system.master_data_items (
    id, master_data_id, title, code, description, created_by, created_on, last_modified_by,
    last_modified_on, active, deleted, remark, version
)
SELECT seed.id, parent.id, seed.title, seed.code, seed.description, 'SYSTEM', NOW(), 'SYSTEM',
       NOW(), true, false, seed.remark, 1
FROM seed
JOIN LATERAL (
    SELECT id
    FROM system.master_data
    WHERE UPPER(code) = seed.parent_code AND active = true AND deleted = false
    ORDER BY id
    LIMIT 1
) parent ON true
WHERE NOT EXISTS (
    SELECT 1
    FROM system.master_data_items existing
    WHERE existing.master_data_id = parent.id
      AND (UPPER(existing.code) = seed.code OR LOWER(existing.title) = LOWER(seed.title))
)
ON CONFLICT DO NOTHING;
