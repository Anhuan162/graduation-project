-- Add storage_path column and change size to BIGINT for file_metadata table
ALTER TABLE file_metadata ADD COLUMN storage_path VARCHAR(500);
ALTER TABLE file_metadata ALTER COLUMN size TYPE BIGINT;
