ALTER TABLE posts ADD COLUMN IF NOT EXISTS reaction_count INTEGER;
ALTER TABLE posts ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN;
ALTER TABLE topics ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN;

ALTER TABLE comments ADD COLUMN IF NOT EXISTS reaction_count INTEGER;
ALTER TABLE comments ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN;

CREATE TABLE IF NOT EXISTS reactions
(
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID        NOT NULL,

    target_id   UUID        NOT NULL,
    target_type VARCHAR(20) NOT NULL CHECK (target_type IN ('POST', 'COMMENT')),
    type        VARCHAR(20) NOT NULL CHECK (type IN ('LIKE', 'LOVE', 'HAHA', 'WOW', 'SAD', 'ANGRY')),
    created_at  TIMESTAMPTZ      DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_reactions_unique_interaction UNIQUE (user_id, target_id, target_type),
    CONSTRAINT fk_reactions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
CREATE INDEX idx_reactions_target ON reactions (target_id, target_type);


CREATE TABLE IF NOT EXISTS reports
(
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    reporter_id UUID        NOT NULL,

    -- Nội dung báo cáo
    reason      VARCHAR(50) NOT NULL,
    description TEXT,
    status      VARCHAR(50)      DEFAULT 'PENDING',

    target_type VARCHAR(50) NOT NULL,
    post_id     UUID,
    comment_id  UUID,

    ip_address  VARCHAR(255),
    created_at  TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

ALTER TABLE reports
    ADD CONSTRAINT fk_reports_reporter
        FOREIGN KEY (reporter_id) REFERENCES users (id);

ALTER TABLE reports
    ADD CONSTRAINT fk_reports_post
        FOREIGN KEY (post_id) REFERENCES posts (id)
            ON DELETE CASCADE;

ALTER TABLE reports
    ADD CONSTRAINT fk_reports_comment
        FOREIGN KEY (comment_id) REFERENCES comments (id)
            ON DELETE CASCADE;

CREATE INDEX idx_report_status ON reports (status);
CREATE INDEX idx_report_created_at ON reports (created_at);

ALTER TABLE reports
    ADD CONSTRAINT chk_valid_target_report
        CHECK (
            (target_type = 'POST' AND post_id IS NOT NULL AND comment_id IS NULL)
                OR
            (target_type = 'COMMENT' AND comment_id IS NOT NULL AND post_id IS NULL)
            );