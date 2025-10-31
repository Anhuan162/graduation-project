CREATE TABLE semesters
(
    id            VARCHAR(255) PRIMARY KEY,
    semester_type VARCHAR(255),
    school_year   INTEGER,
    CONSTRAINT semesters_semester_type_school_year_unique UNIQUE (semester_type, school_year)
);

ALTER TABLE subject_references DROP COLUMN IF EXISTS semester_type;
ALTER TABLE subject_references
    ADD COLUMN semester_id VARCHAR (255);
ALTER TABLE subject_references
    ADD CONSTRAINT subject_references_semester_id_fkey FOREIGN KEY (semester_id)
    REFERENCES semesters (id) ON
DELETE
SET NULL;

