ALTER TABLE posts ADD COLUMN slug VARCHAR(255);

-- Handle any potential duplicate slugs by setting them to NULL to avoid UNIQUE constraint violation
UPDATE posts SET slug = NULL WHERE slug IN (
    SELECT slug FROM posts WHERE slug IS NOT NULL GROUP BY slug HAVING COUNT(*) > 1
);

-- Create unique index on slug column
CREATE UNIQUE INDEX idx_post_slug ON posts(slug);
