-- Add processed_by_id and processed_at to reports table
ALTER TABLE reports
ADD COLUMN IF NOT EXISTS processed_by_id UUID,
ADD COLUMN IF NOT EXISTS processed_at TIMESTAMPTZ;

-- Add foreign key constraint for processed_by_id
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'fk_reports_processed_by'
  ) THEN
    ALTER TABLE reports
      ADD CONSTRAINT fk_reports_processed_by
      FOREIGN KEY (processed_by_id) REFERENCES users(id);
  END IF;
END $$;

-- Add index for processed_by_id
CREATE INDEX IF NOT EXISTS ix_reports_processed_by
    ON reports(processed_by_id);
