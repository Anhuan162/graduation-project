CREATE TABLE public.cpa_profiles
(
    id                        UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cpa_profile_name          VARCHAR(255) NOT NULL,
    cpa_profile_code          VARCHAR(100) NOT NULL UNIQUE,
    letter_cpa_score          VARCHAR(10),
    number_cpa_score          NUMERIC(5, 3),
    previous_number_cpa_score NUMERIC(5, 3),
    total_accumulated_score   NUMERIC(5, 3),
    accumulated_credits       INTEGER,
    user_id                   UUID         NOT NULL,
    CONSTRAINT cpa_profiles_user_id_fkey
        FOREIGN KEY (user_id)
            REFERENCES public.users (id) ON DELETE CASCADE
);
CREATE INDEX idx_cpa_profiles_name
    ON public.cpa_profiles (cpa_profile_name);


CREATE TABLE public.gpa_profiles
(
    id                        UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    gpa_profile_code          VARCHAR(100) NOT NULL UNIQUE,
    letter_gpa_score          VARCHAR(10),
    number_gpa_score          NUMERIC(5, 3),
    previous_number_gpa_score NUMERIC(5, 3),
    total_weighted_score      NUMERIC(5, 3),
    passed_credits            INTEGER,
    cpa_profile_id            UUID         NOT NULL,
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
    letter_current_score     VARCHAR(10)   ,
    letter_improvement_score VARCHAR(10)   ,
    current_score            NUMERIC(5, 3) ,
    improvement_score        NUMERIC(5, 3) ,
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



