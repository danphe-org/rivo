CREATE TABLE IF NOT EXISTS master_data (
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

CREATE INDEX IF NOT EXISTS ix_master_data_code ON master_data(code);
CREATE INDEX IF NOT EXISTS ix_master_data_title ON master_data(title);
CREATE INDEX IF NOT EXISTS ix_master_data_created_on ON master_data(created_on DESC);
