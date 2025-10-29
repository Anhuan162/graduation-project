CREATE
EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE public.users
(
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    avatar_url   VARCHAR(255),
    email        VARCHAR(255),
    enabled      BOOLEAN,
    full_name    VARCHAR(255),
    password     VARCHAR(255),
    phone        VARCHAR(255),
    provider     VARCHAR(50) NOT NULL,
    student_code VARCHAR(255)
);


CREATE TABLE public.announcements
(
    id                  UUID NOT NULL,
    announcement_status BOOLEAN     DEFAULT NULL,
    announcement_type   VARCHAR(50) DEFAULT NULL,
    content             VARCHAR(255),
    created_date        DATE,
    modified_date       DATE,
    title               VARCHAR(255),
    created_by          UUID,
    modified_by         UUID,
    CONSTRAINT announcements_pkey PRIMARY KEY (id),
    CONSTRAINT announcements_created_by_fkey FOREIGN KEY (created_by)
        REFERENCES public.users (id),
    CONSTRAINT announcements_modified_by_fkey FOREIGN KEY (modified_by)
        REFERENCES public.users (id)
);
CREATE INDEX idx_announcements_created_by ON public.announcements (created_by);
CREATE INDEX idx_announcements_modified_by ON public.announcements (modified_by);


CREATE TABLE public.audit_logs
(
    id         UUID NOT NULL,
    action     VARCHAR(255),
    created_at TIMESTAMP(6),
    ip_address VARCHAR(255),
    user_id    UUID,
    CONSTRAINT audit_log_pkey PRIMARY KEY (id),
    CONSTRAINT audit_log_user_id_fkey FOREIGN KEY (user_id)
        REFERENCES public.users (id)
);
CREATE INDEX idx_audit_log_user_id
    ON public.audit_logs (user_id);


CREATE TABLE public.faculties
(
    id           UUID NOT NULL,
    description  VARCHAR(255),
    faculty_code VARCHAR(255),
    faculty_name VARCHAR(255),
    CONSTRAINT faculties_pkey PRIMARY KEY (id)
);


CREATE TABLE public.classrooms
(
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    class_code       VARCHAR(255),
    class_name       VARCHAR(255),
    ended_year       INTEGER,
    school_year_code VARCHAR(255),
    started_year     INTEGER,
    faculty_id       UUID,
    CONSTRAINT classrooms_faculty_id_fkey FOREIGN KEY (faculty_id)
        REFERENCES public.faculties (id)
);
CREATE INDEX idx_classrooms_faculty_id
    ON public.classrooms (faculty_id);


CREATE TABLE public.announcement_targets
(
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    classroom_code  VARCHAR(255),
    announcement_id UUID,
    CONSTRAINT announcement_targets_announcement_id_fkey FOREIGN KEY (announcement_id)
        REFERENCES public.announcements (id)
);
CREATE INDEX idx_announcement_targets_announcement_id
    ON public.announcement_targets (announcement_id);


CREATE TABLE public.invalidated_tokens
(
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    expiry_time TIMESTAMP(6),
    issued_at   TIMESTAMP(6),
    jit         VARCHAR(255),
    user_id     UUID NOT NULL,
    CONSTRAINT invalidated_tokens_user_id_fkey FOREIGN KEY (user_id)
        REFERENCES public.users (id)
);
CREATE INDEX idx_invalidated_tokens_user_id
    ON public.invalidated_tokens (user_id);


CREATE TABLE public.notification_events
(
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    content    TEXT,
    created_at TIMESTAMP(6),
    related_id UUID,
    title      VARCHAR(255),
    type       VARCHAR(50)      DEFAULT NULL,
    created_by UUID,
    CONSTRAINT notification_events_created_by_fkey FOREIGN KEY (created_by)
        REFERENCES public.users (id)
);
CREATE INDEX idx_notification_events_created_by
    ON public.notification_events (created_by);


CREATE TABLE public.user_notifications
(
    id                    UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    delivered_at          TIMESTAMP(6),
    is_read               BOOLEAN NOT NULL,
    notification_status   VARCHAR(50)      DEFAULT NULL,
    read_at               TIMESTAMP(6),
    notification_event_id UUID,
    user_id               UUID,
    CONSTRAINT user_notifications_event_id_fkey FOREIGN KEY (notification_event_id)
        REFERENCES public.notification_events (id),
    CONSTRAINT user_notifications_user_id_fkey FOREIGN KEY (user_id)
        REFERENCES public.users (id)
);
CREATE INDEX idx_user_notifications_event_id
    ON public.user_notifications (notification_event_id);
CREATE INDEX idx_user_notifications_user_id
    ON public.user_notifications (user_id);


CREATE TABLE public.oauth_accounts
(
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    access_token     VARCHAR(255),
    expires_at       TIMESTAMP(6),
    provider         VARCHAR(50)      DEFAULT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    refresh_token    VARCHAR(255),
    user_id          UUID         NOT NULL,
    CONSTRAINT oauth_accounts_user_id_fkey FOREIGN KEY (user_id)
        REFERENCES public.users (id)
);
CREATE INDEX idx_oauth_accounts_user_id
    ON public.oauth_accounts (user_id);


CREATE TABLE public.roles
(
    name        VARCHAR(255) PRIMARY KEY,
    description VARCHAR(255)
);


CREATE TABLE public.permissions
(
    name            VARCHAR(255) PRIMARY KEY,
    permission_type VARCHAR(50) DEFAULT NULL,
    resource_type   VARCHAR(50) DEFAULT NULL,
    CONSTRAINT permissions_resource_permission_unique UNIQUE (resource_type, permission_type)
);


CREATE TABLE public.roles_permissions
(
    role_name        VARCHAR(255) NOT NULL,
    permissions_name VARCHAR(255) NOT NULL,
    CONSTRAINT roles_permissions_pkey PRIMARY KEY (role_name, permissions_name),
    CONSTRAINT roles_permissions_role_name_fkey FOREIGN KEY (role_name)
        REFERENCES public.roles (name),
    CONSTRAINT roles_permissions_permissions_name_fkey FOREIGN KEY (permissions_name)
        REFERENCES public.permissions (name)
);
CREATE INDEX idx_roles_permissions_permissions_name
    ON public.roles_permissions (permissions_name);


CREATE TABLE public.user_roles
(
    user_id UUID         NOT NULL,
    role_id VARCHAR(255) NOT NULL,
    CONSTRAINT user_roles_pkey PRIMARY KEY (user_id, role_id),
    CONSTRAINT user_roles_user_id_fkey FOREIGN KEY (user_id)
        REFERENCES public.users (id),
    CONSTRAINT user_roles_role_id_fkey FOREIGN KEY (role_id)
        REFERENCES public.roles (name)
);
CREATE INDEX idx_user_roles_role_id
    ON public.user_roles (role_id);


CREATE TABLE public.verification_tokens
(
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    expiry_date TIMESTAMP(6),
    token       VARCHAR(255),
    user_id     UUID,
    CONSTRAINT verification_tokens_user_id_key UNIQUE (user_id),
    CONSTRAINT verification_tokens_user_id_fkey FOREIGN KEY (user_id)
        REFERENCES public.users (id)
);


