CREATE TABLE IF NOT EXISTS match_requests (
    id               UUID PRIMARY KEY,
    requested_by     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    request_type     VARCHAR(50) NOT NULL,
    status           VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    game             VARCHAR(100) NOT NULL,
    category         VARCHAR(100),
    message          TEXT,
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

CREATE INDEX IF NOT EXISTS ix_match_requests_requested_by ON match_requests(requested_by);
CREATE INDEX IF NOT EXISTS ix_match_requests_request_type ON match_requests(request_type);
CREATE INDEX IF NOT EXISTS ix_match_requests_status ON match_requests(status);
CREATE INDEX IF NOT EXISTS ix_match_requests_game ON match_requests(game);
CREATE INDEX IF NOT EXISTS ix_match_requests_created_on ON match_requests(created_on DESC);
