ALTER TABLE vacancies
    ADD COLUMN last_seen_at TIMESTAMP,
    ADD COLUMN closed_at TIMESTAMP;

UPDATE vacancies
SET last_seen_at = ingested_at
WHERE last_seen_at IS NULL;

ALTER TABLE vacancies
    ALTER COLUMN last_seen_at SET NOT NULL;
