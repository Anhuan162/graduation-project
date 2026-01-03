-- Increase the size of the url column in file_metadata table to accommodate full Firebase Storage URLs
ALTER TABLE file_metadata ALTER COLUMN url TYPE VARCHAR(1000);
