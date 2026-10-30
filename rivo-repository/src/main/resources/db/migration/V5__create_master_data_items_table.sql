CREATE TABLE IF NOT EXISTS master_data_items (
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
        REFERENCES master_data(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_master_data_items_master_data_id ON master_data_items(master_data_id);
CREATE INDEX IF NOT EXISTS ix_master_data_items_code ON master_data_items(code);
CREATE INDEX IF NOT EXISTS ix_master_data_items_title ON master_data_items(title);
CREATE INDEX IF NOT EXISTS ix_master_data_items_created_on ON master_data_items(created_on DESC);
CREATE UNIQUE INDEX IF NOT EXISTS ux_master_data_items_parent_title
    ON master_data_items (master_data_id, LOWER(title))
    WHERE deleted = FALSE;
