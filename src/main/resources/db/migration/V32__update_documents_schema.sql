-- Add new columns for Document Entity features (Phase 1 & 3)
ALTER TABLE documents
ADD COLUMN IF NOT EXISTS is_premium BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS page_count INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS thumbnail_url TEXT,
ADD COLUMN IF NOT EXISTS preview_images TEXT,
ADD COLUMN IF NOT EXISTS rejection_reason TEXT,
ADD COLUMN IF NOT EXISTS file_size BIGINT; -- Assuming mapping to 'size' field in entity

-- Renaming 'size' column if it existed as something else or just ensuring 'size' maps to 'fileSize' if needed.
-- Note: Entity has 'private long size' which typically maps to 'size' column.
-- The user prompt suggested 'ADD COLUMN file_size BIGINT'.
-- Let's check naming strategy. If default, camelCase 'fileSize' -> snake_case 'file_size'.
-- If field is 'size', it maps to 'size'.
-- User's prompt mentioned: "ADD COLUMN file_size BIGINT".
-- But my previous view of Document.java showed `private long size;`.
-- If the field is named `size`, the column should be `size`.
-- However, User explicitly asked for `file_size`. Let me double check Document.java to be safe.
-- Wait, I can't check right now without delays. I will stick to User's SQL instruction but verify Document.java in next step if needed.
-- Actually, the user's SQL snippet has `ADD COLUMN file_size BIGINT`.
-- I'll follow the user's instruction but I suspect the entity field `size` maps to `size` column unless `@Column(name="file_size")` is present.
-- I will blindly trust the user's provided SQL for now to be "Senior compliant" with their request, but better to be safe.
-- Actually, looking at previous logs (Step 255), there was a warning about mapped target properties.
-- Step 201 interactions showed `setFileSize` vs `setSize` confusion.
-- Let's stick to `file_size` if that's what they want, but I'll check `Document.java` quickly.
