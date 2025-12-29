CREATE TABLE file_metadata
(
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    file_name     VARCHAR(255) NOT NULL,
    folder        VARCHAR(255),
    url           VARCHAR(500),
    content_type  VARCHAR(100),
    size          INT,
    access_type   VARCHAR(50),
    resource_type VARCHAR(50),
    resource_id   UUID,
    created_at    TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    user_id       UUID,

    CONSTRAINT fk_file_metadata_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);
