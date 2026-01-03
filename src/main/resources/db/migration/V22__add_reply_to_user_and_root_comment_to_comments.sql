-- Add reply_to_user_id and root_comment_id to comments

ALTER TABLE comments
  ADD COLUMN IF NOT EXISTS reply_to_user_id UUID NULL,
  ADD COLUMN IF NOT EXISTS root_comment_id UUID NULL;

-- FK to users (reply_to_user_id)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_comments_reply_to_user'
  ) THEN
    ALTER TABLE comments
      ADD CONSTRAINT fk_comments_reply_to_user
      FOREIGN KEY (reply_to_user_id) REFERENCES users(id)
      ON DELETE SET NULL;
  END IF;
END $$;

-- FK to comments (root_comment_id)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_comments_root_comment'
  ) THEN
    ALTER TABLE comments
      ADD CONSTRAINT fk_comments_root_comment
      FOREIGN KEY (root_comment_id) REFERENCES comments(id)
      ON DELETE SET NULL;
  END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_comments_reply_to_user_id ON comments(reply_to_user_id);
CREATE INDEX IF NOT EXISTS idx_comments_root_comment_id ON comments(root_comment_id);
