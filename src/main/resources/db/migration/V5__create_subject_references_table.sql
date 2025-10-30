CREATE TABLE subject_references
(
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    semester_type VARCHAR(50),
    subject_id    UUID NOT NULL,
    faculty_id    UUID NOT NULL,

    CONSTRAINT subject_references_subject_id_fkey
        FOREIGN KEY (subject_id)
            REFERENCES public.subjects (id)
            ON DELETE CASCADE,

    CONSTRAINT subject_references_faculty_id_fkey
        FOREIGN KEY (faculty_id)
            REFERENCES public.faculties (id)
            ON DELETE CASCADE
);
CREATE INDEX idx_subject_references_subject_id ON subject_references (subject_id);
CREATE INDEX idx_subject_references_faculty_id ON subject_references (faculty_id);