ALTER TABLE match_requests
    ADD COLUMN IF NOT EXISTS "time" TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS urgent BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS ix_match_requests_time ON match_requests("time");
CREATE INDEX IF NOT EXISTS ix_match_requests_urgent ON match_requests(urgent);
