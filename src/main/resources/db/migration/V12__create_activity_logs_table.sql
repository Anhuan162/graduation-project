CREATE TABLE activity_logs
(
    id          UUID PRIMARY KEY      DEFAULT uuid_generate_v4(),
    user_id     UUID NULL,             -- người thực hiện hành động
    action      VARCHAR(100) NOT NULL, -- CREATE_ANNOUNCEMENT, UPDATE_CLASSROOM,...
    module      VARCHAR(100) NOT NULL, -- ANNOUNCEMENT, CLASSROOM, USER, FILE,...
    description TEXT NULL,             -- mô tả dễ hiểu để hiển thị UI
    target_id   UUID NULL,             -- ID của đối tượng bị tác động
    target_type VARCHAR(100) NULL,     -- ANNOUNCEMENT, CLASSROOM, USER, FILE
    metadata    VARCHAR(255) NULL,     -- extra fields: title, oldValue, newValue,...
    ip_address  VARCHAR(50) NULL,
    user_agent  VARCHAR(255) NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
