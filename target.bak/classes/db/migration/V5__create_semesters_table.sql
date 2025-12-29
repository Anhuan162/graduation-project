CREATE TABLE semesters
(
    id            INTEGER PRIMARY KEY,
    semester_type VARCHAR(255),
    school_year   INTEGER,
    CONSTRAINT semesters_semester_type_school_year_unique UNIQUE (semester_type, school_year)
);


