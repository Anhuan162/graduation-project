ALTER TABLE reports
ADD COLUMN topic_id UUID,
ADD CONSTRAINT fk_reports_topic FOREIGN KEY (topic_id) REFERENCES topics (id) ON DELETE CASCADE;
