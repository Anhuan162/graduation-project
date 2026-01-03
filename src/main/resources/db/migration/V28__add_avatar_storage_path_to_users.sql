-- Add avatar_storage_path column to users table
ALTER TABLE users ADD COLUMN avatar_storage_path VARCHAR(500);
