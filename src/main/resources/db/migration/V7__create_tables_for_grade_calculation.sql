
CREATE TABLE public.cpa_profiles
(
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cpa_profile_name    VARCHAR(255)  NOT NULL,
    cpa_profile_code    VARCHAR(100)  NOT NULL UNIQUE,
    letter_cpa_score    NUMERIC(2, 3) NOT NULL,
    number_cpa_score    NUMERIC(2, 3) NOT NULL,
    accumulated_credits INTEGER       NOT NULL
);
CREATE INDEX idx_cpa_profiles_name
    ON public.cpa_profiles (cpa_profile_name);

-
CREATE TABLE public.gpa_profiles
(
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    gpa_profile_code VARCHAR(100)  NOT NULL UNIQUE,
    letter_gpa_score NUMERIC(2, 3) NOT NULL,
    number_gpa_score NUMERIC(2, 3) NOT NULL,
    passed_credits   INTEGER       NOT NULL,
    cpa_profile_id   UUID          NOT NULL,
    CONSTRAINT gpa_profiles_cpa_profile_id_fkey
        FOREIGN KEY (cpa_profile_id)
            REFERENCES public.cpa_profiles (id) ON DELETE CASCADE
);
CREATE INDEX idx_gpa_profiles_gpa_profile_code
    ON public.gpa_profiles (gpa_profile_code);
CREATE INDEX idx_gpa_profiles_cpa_profile_id
    ON public.gpa_profiles (cpa_profile_id);


CREATE TABLE public.grade_subject_average_profiles
(
    id                       UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    letter_current_score     VARCHAR(10)   NOT NULL,
    letter_improvement_score VARCHAR(10)   NOT NULL,
    credit_units             INTEGER       NOT NULL,
    current_score            NUMERIC(2, 3) NOT NULL,
    improvement_score        NUMERIC(2, 3) NOT NULL,
    gpa_profile_id           UUID          NOT NULL,
    subject_reference_id     UUID          NOT NULL,
    CONSTRAINT grade_subject_avg_profiles_gpa_profile_id_fkey
        FOREIGN KEY (gpa_profile_id)
            REFERENCES public.gpa_profiles (id) ON DELETE CASCADE,
    CONSTRAINT grade_subject_avg_profiles_subject_reference_id_fkey
        FOREIGN KEY (subject_reference_id)
            REFERENCES public.subject_references (id) ON DELETE CASCADE
);
CREATE INDEX idx_grade_subject_avg_profiles_gpa_profile_id
    ON public.grade_subject_average_profiles (gpa_profile_id);
CREATE INDEX idx_grade_subject_avg_profiles_subject_reference_id
    ON public.grade_subject_average_profiles (subject_reference_id);



