ALTER TABLE client.match_requests ADD COLUMN IF NOT EXISTS location VARCHAR(255);
CREATE INDEX IF NOT EXISTS ix_match_requests_location ON client.match_requests(location);
CREATE OR REPLACE VIEW public.match_requests AS SELECT * FROM client.match_requests;
