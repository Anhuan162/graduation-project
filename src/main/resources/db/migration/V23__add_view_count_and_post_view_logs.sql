-- 1) Add view_count to posts
ALTER TABLE posts
ADD COLUMN IF NOT EXISTS view_count BIGINT NOT NULL DEFAULT 0;

-- 2) Create post_view_logs table
CREATE TABLE IF NOT EXISTS post_view_logs (
    id UUID PRIMARY KEY,
    post_id UUID NOT NULL,
    viewer_key VARCHAR(128) NOT NULL,
    first_viewed_at TIMESTAMPTZ NOT NULL,
    last_viewed_at  TIMESTAMPTZ NOT NULL,
    view_count BIGINT NOT NULL DEFAULT 1,

    CONSTRAINT fk_post_view_logs_post
        FOREIGN KEY (post_id) REFERENCES posts(id)
);

-- 3) Indexes for post_view_logs
CREATE UNIQUE INDEX IF NOT EXISTS ux_post_view_logs_post_viewer
    ON post_view_logs(post_id, viewer_key);

CREATE INDEX IF NOT EXISTS ix_post_view_logs_post
    ON post_view_logs(post_id);

-- 4) Unique constraint to prevent duplicate reactions
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'uk_reactions_user_target_type'
  ) THEN
    ALTER TABLE reactions
      ADD CONSTRAINT uk_reactions_user_target_type
      UNIQUE (user_id, target_id, target_type);
  END IF;
END $$;
