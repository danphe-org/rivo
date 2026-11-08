ALTER TABLE client.match_requests ADD COLUMN IF NOT EXISTS players_needed INT;
CREATE OR REPLACE VIEW public.match_requests AS SELECT * FROM client.match_requests;
