ALTER TABLE topics ADD COLUMN slug VARCHAR(255);

-- Handle any potential duplicate slugs by setting them to NULL to avoid UNIQUE constraint violation
UPDATE topics SET slug = NULL WHERE slug IN (
    SELECT slug FROM topics WHERE slug IS NOT NULL GROUP BY slug HAVING COUNT(*) > 1
);

-- Create unique index on slug column
CREATE UNIQUE INDEX idx_topic_slug ON topics(slug);
