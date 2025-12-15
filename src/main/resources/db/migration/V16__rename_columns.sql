ALTER TABLE posts
    RENAME COLUMN is_deleted TO deleted;

ALTER TABLE topics
    RENAME COLUMN is_deleted TO deleted;
