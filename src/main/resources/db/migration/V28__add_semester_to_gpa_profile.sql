-- V28: Add Semester Relationship to GpaProfile
-- Purpose: Normalize GpaProfile by adding FK to Semester
-- Fixes: Handles unique constraint conflicts on semesters table

-- 1. Add Column
ALTER TABLE gpa_profiles ADD COLUMN semester_id INTEGER;

-- 2. SEED MISSING SEMESTERS (Robust Fix)
-- We need IDs 1-20 to exist because gpa_profiles references them.
-- Previous attempt failed because calculated years (e.g. 2023) conflicted with existing data.
-- New Strategy: Use placeholder years (3000+) to avoid conflicts with real data.
-- If ID already exists, we do nothing (ON CONFLICT DO NOTHING).
-- If (type, year) constraint conflicts, it means we have weird data, but 3000+ should be safe.

INSERT INTO semesters (id, semester_type, school_year)
SELECT 
    i, 
    CASE WHEN i % 2 != 0 THEN 'FIRST' ELSE 'SECOND' END, 
    3000 + (i + 1) / 2
FROM generate_series(1, 20) as i
ON CONFLICT (id) DO NOTHING;

-- 3. Backfill Data
-- Strategy: Strict Single-Digit Extraction
-- "GPAB21...6181" -> "1"
-- "GPAB21...618A" -> NULL -> Default to 1
UPDATE gpa_profiles
SET semester_id = COALESCE(
    CAST(SUBSTRING(gpa_profile_code FROM '\d$') AS INTEGER),
    1
)
WHERE semester_id IS NULL;

-- 4. Enforce Constraints
ALTER TABLE gpa_profiles 
    ALTER COLUMN semester_id SET NOT NULL,
    ADD CONSTRAINT fk_gpa_profile_semester FOREIGN KEY (semester_id) REFERENCES semesters(id);

CREATE INDEX idx_gpa_profiles_semester_id ON gpa_profiles(semester_id);
