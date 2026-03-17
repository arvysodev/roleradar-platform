ALTER TABLE vacancies
    ADD COLUMN description_html text,
    ADD COLUMN description_text text;

UPDATE vacancies
SET description_html = description,
    description_text = description
WHERE description IS NOT NULL;

ALTER TABLE vacancies
    ALTER COLUMN description_html SET NOT NULL,
    ALTER COLUMN description_text SET NOT NULL;