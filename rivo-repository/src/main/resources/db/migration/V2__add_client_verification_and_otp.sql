ALTER TABLE client.client_users ADD COLUMN IF NOT EXISTS verified BOOLEAN NOT NULL DEFAULT FALSE;
UPDATE client.client_users SET verified = TRUE;

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
