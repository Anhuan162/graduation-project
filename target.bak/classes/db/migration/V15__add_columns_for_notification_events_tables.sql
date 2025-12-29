ALTER TABLE notification_events
    ADD COLUMN IF NOT EXISTS parent_reference_id UUID;
ALTER TABLE notification_events
    ADD COLUMN IF NOT EXISTS reference_id UUID;

ALTER TABLE topics
    ADD COLUMN IF NOT EXISTS lastModifiedAt TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW();

