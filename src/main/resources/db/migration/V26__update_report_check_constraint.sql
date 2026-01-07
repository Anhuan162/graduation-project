ALTER TABLE reports
DROP CONSTRAINT chk_valid_target_report;

ALTER TABLE reports
ADD CONSTRAINT chk_valid_target_report CHECK (
    (target_type = 'POST' AND post_id IS NOT NULL AND comment_id IS NULL AND topic_id IS NULL) OR
    (target_type = 'COMMENT' AND comment_id IS NOT NULL AND post_id IS NULL AND topic_id IS NULL) OR
    (target_type = 'TOPIC' AND topic_id IS NOT NULL AND post_id IS NULL AND comment_id IS NULL)
);
