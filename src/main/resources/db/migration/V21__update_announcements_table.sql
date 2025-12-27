ALTER TABLE announcements
    ADD COLUMN IF NOT EXISTS source_url VARCHAR (255);
ALTER TABLE announcements
    ADD COLUMN IF NOT EXISTS announcement_provider VARCHAR (50);

ALTER TABLE announcements ALTER COLUMN content TYPE TEXT;
ALTER TABLE announcements ALTER COLUMN title TYPE TEXT;

