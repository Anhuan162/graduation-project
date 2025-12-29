CREATE TABLE subjects
(
    id                 UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    subject_name       VARCHAR(255) NOT NULL,
    subject_code       VARCHAR(50),
    credit             INTEGER,
    description        TEXT,
    created_date       TIMESTAMP,
    last_modified_date TIMESTAMP
);

CREATE INDEX idx_subjects_subject_code ON subjects(subject_code);
CREATE INDEX idx_subjects_subject_name ON subjects(subject_name);
