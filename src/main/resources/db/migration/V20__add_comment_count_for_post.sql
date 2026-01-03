ALTER TABLE posts 
ADD COLUMN comment_count BIGINT NOT NULL DEFAULT 0;

UPDATE posts p
SET comment_count = (
    SELECT COUNT(*) 
    FROM comments c 
    WHERE c.post_id = p.id 
    AND c.deleted = false 
);