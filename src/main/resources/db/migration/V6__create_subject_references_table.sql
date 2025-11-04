CREATE TABLE subject_references
(
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    semester_type VARCHAR(50),
    subject_id    UUID         NOT NULL,
    faculty_id    UUID         NOT NULL,
    semester_id   INTEGER      NOT NULL,
    cohort_code   VARCHAR(255) NOT NULL,

    CONSTRAINT subject_references_subject_id_fkey
        FOREIGN KEY (subject_id)
            REFERENCES public.subjects (id)
            ON DELETE CASCADE,

    CONSTRAINT subject_references_faculty_id_fkey
        FOREIGN KEY (faculty_id)
            REFERENCES public.faculties (id)
            ON DELETE CASCADE,

    CONSTRAINT subject_references_semester_id_fkey
        FOREIGN KEY (semester_id)
            REFERENCES semesters (id)
            ON DELETE CASCADE,

    CONSTRAINT subject_references_subject_faculty_semester_unique UNIQUE (subject_id, faculty_id, semester_id)
);
CREATE INDEX idx_subject_references_subject_id ON subject_references (subject_id);
CREATE INDEX idx_subject_references_faculty_id ON subject_references (faculty_id);
CREATE INDEX idx_subject_references_semester_id ON subject_references (semester_id);