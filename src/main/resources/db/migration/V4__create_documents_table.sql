CREATE TABLE public.documents
(
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title             VARCHAR(255),
    description       TEXT,
    file_path         TEXT,
    document_status   VARCHAR(50),
    document_type     VARCHAR(50),
    size              INTEGER,
    original_filename VARCHAR(255),
    storage_provider  VARCHAR(255),
    mime_type         VARCHAR(100),
    checksum          VARCHAR(255),
    visibility        VARCHAR(50),
    download_count    INTEGER          DEFAULT 0,
    created_at        TIMESTAMP,
    updated_at        TIMESTAMP,
    approved_at       TIMESTAMP,
    deleted_at        TIMESTAMP,
    subject_id        UUID,
    uploaded_by       UUID,
    approved_by       UUID,
    CONSTRAINT documents_subject_id_fkey FOREIGN KEY (subject_id)
        REFERENCES public.subjects (id) ON DELETE CASCADE,
    CONSTRAINT documents_uploaded_by_fkey FOREIGN KEY (uploaded_by)
        REFERENCES public.users (id) ON DELETE SET NULL,
    CONSTRAINT documents_approved_by_fkey FOREIGN KEY (approved_by)
        REFERENCES public.users (id) ON DELETE SET NULL
);
CREATE INDEX idx_documents_subject_id ON documents (subject_id);
CREATE INDEX idx_documents_uploaded_by ON documents (uploaded_by);
CREATE INDEX idx_documents_approved_by ON documents (approved_by);
