-- Migration: Add case-insensitive unique constraint for document titles per subject
-- Version: V27
-- Author: zayn-hargreaves
-- Date: 2026-01-08
-- Decision: Per-subject uniqueness + Case-insensitive (MANDATORY)
-- Sanity Check Result: 0 case-insensitive duplicates found (2026-01-08)

-- CRITICAL: This migration has been verified safe to run.
-- Sanity check query was executed and returned 0 duplicates:
--   SELECT LOWER(title), subject_id, COUNT(*) 
--   FROM documents 
--   GROUP BY LOWER(title), subject_id 
--   HAVING COUNT(*) > 1;
-- Result: (0 rows)

-- Create case-insensitive unique index
-- This prevents both "Toán cao cấp.pdf" AND "TOÁN CAO CẤP.pdf" from existing in the same subject
CREATE UNIQUE INDEX uk_document_title_subject_ci 
ON documents (LOWER(title), subject_id);

-- Additional index for query performance optimization
CREATE INDEX IF NOT EXISTS idx_documents_title_lower ON documents(LOWER(title));

-- Migration complete
-- Source of truth: Database-level constraint prevents race conditions
