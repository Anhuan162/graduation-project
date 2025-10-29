INSERT INTO users
(id, avatar_url, email, enabled, full_name, password, phone, provider, student_code)
VALUES ('417c9992-8bd1-41f2-852e-54b3e7ca7667', NULL, 'huanmanutd2k3@gmail.com', true,
        NULL, '$2a$10$vSOxFQ8Qr2i7421Q8C3rvu0HkY0fQQ/tT0xM2WtJ/2V4/YECL9GRO', NULL, 'LOCAL', NULL);
INSERT INTO users
(id, avatar_url, email, enabled, full_name, password, phone, provider, student_code)
VALUES ('4af76b85-6971-47b1-a4aa-0f83da58f2f5', NULL, 'huan1622k3@gmail.com', true,
        NULL, '$2a$10$eezkDqG.gNQKxeEXHJrqquDhVmJVTsMc5GFxkCOpEdrqDm1.NOayG', NULL, 'LOCAL', NULL);
INSERT INTO users
(id, avatar_url, email, enabled, full_name, password, phone, provider, student_code)
VALUES ('c264acd5-a212-4f97-9a85-e68da8b48e38', NULL, 'huan162182143@gmail.com', true,
        NULL, '$2a$10$WeX1Zf55Vb5j7qiaV5YA4OZn8efuABvdcuKrWKvC.5qYf0LvzURtW', NULL, 'LOCAL', NULL);


INSERT INTO roles
    (name, description)
VALUES ('ADMIN', NULL);
INSERT INTO roles
    (name, description)
VALUES ('USER', NULL);


INSERT INTO permissions
    (name, permission_type, resource_type)
VALUES ('CREATE_ALL_PERMISSIONS', 'create_all', 'PERMISSION');
INSERT INTO permissions
    (name, permission_type, resource_type)
VALUES ('READ_ALL_PERMISSIONS', 'read_all', 'PERMISSION');
INSERT INTO permissions
    (name, permission_type, resource_type)
VALUES ('UPDATE_ALL_PERMISSIONS', 'update_all', 'PERMISSION');
INSERT INTO permissions
    (name, permission_type, resource_type)
VALUES ('CREATE_ALL_ROLES', 'create_all', 'ROLE');
INSERT INTO permissions
    (name, permission_type, resource_type)
VALUES ('READ_ALL_USERS', 'read_all', 'USER');


INSERT INTO roles_permissions
    (role_name, permissions_name)
VALUES ('ADMIN', 'CREATE_ALL_PERMISSIONS');
INSERT INTO roles_permissions
    (role_name, permissions_name)
VALUES ('ADMIN', 'CREATE_ALL_ROLES');
INSERT INTO roles_permissions
    (role_name, permissions_name)
VALUES ('ADMIN', 'READ_ALL_PERMISSIONS');
INSERT INTO roles_permissions
    (role_name, permissions_name)
VALUES ('ADMIN', 'READ_ALL_USERS');
INSERT INTO roles_permissions
    (role_name, permissions_name)
VALUES ('ADMIN', 'UPDATE_ALL_PERMISSIONS');


INSERT INTO user_roles
    (user_id, role_id)
VALUES ('4af76b85-6971-47b1-a4aa-0f83da58f2f5', 'ADMIN');
INSERT INTO user_roles
    (user_id, role_id)
VALUES ('417c9992-8bd1-41f2-852e-54b3e7ca7667', 'USER');
INSERT INTO user_roles
    (user_id, role_id)
VALUES ('c264acd5-a212-4f97-9a85-e68da8b48e38', 'USER');


INSERT INTO faculties
    (id, description, faculty_code, faculty_name)
VALUES ('338cb0c5-6d08-4de9-919c-1595a31dd76f', 'An toàn thông tin', 'AT',
        'Information Security');
INSERT INTO faculties
    (id, description, faculty_code, faculty_name)
VALUES ('e94e3fe5-33d1-4b55-bf1d-771fab5ed233',
        'Responsible for computer science, software engineering, and related programs.', 'CN',
        'Information Technology');


INSERT INTO invalidated_tokens
    (id, expiry_time, issued_at, jit, user_id)
VALUES ('03ca3ca3-8aa6-46ac-8603-8739024730a1', '2025-11-07 18:36:08', '2025-10-28 08:36:08',
        'ef401785-ca91-41b2-b120-a76b8661d870', '4af76b85-6971-47b1-a4aa-0f83da58f2f5');
INSERT INTO invalidated_tokens
    (id, expiry_time, issued_at, jit, user_id)
VALUES ('3752390e-c704-49a2-9ee6-2940feee361f', '2025-10-28 20:04:15', '2025-10-28 15:54:15',
        'aac70028-dd52-44f9-bfc5-475aca2d73ad', '4af76b85-6971-47b1-a4aa-0f83da58f2f5');
INSERT INTO invalidated_tokens
    (id, expiry_time, issued_at, jit, user_id)
VALUES ('3c5ddb74-d634-45dc-a75a-bd0c9b9e24a4', '2025-10-30 18:57:31', '2025-10-20 08:57:31',
        '81f1471c-bcce-4749-801c-686fb21e5533', '4af76b85-6971-47b1-a4aa-0f83da58f2f5');
INSERT INTO invalidated_tokens
    (id, expiry_time, issued_at, jit, user_id)
VALUES ('87e3cdb6-60c7-44ee-8bbf-478d57880dea', '2044-12-27 15:06:27', '2025-10-28 15:06:27',
        'dce9ee7e-01af-405b-a054-c6da3a022681', '4af76b85-6971-47b1-a4aa-0f83da58f2f5');
INSERT INTO invalidated_tokens
    (id, expiry_time, issued_at, jit, user_id)
VALUES ('997851ef-bb64-4a82-bbfd-abcc04f9841d', '2025-11-07 18:36:08', '2025-10-28 08:36:08',
        'ef401785-ca91-41b2-b120-a76b8661d870', '4af76b85-6971-47b1-a4aa-0f83da58f2f5');


INSERT INTO classrooms
(id, class_code, class_name, ended_year, school_year_code, started_year, faculty_id)
VALUES ('053766db-c77c-4f48-9727-0484e6c32e35', 'D21CQCN06-B',
        'Công nghệ thông tin 6 khoá 21', 2026, 'D21', 2021,
        'e94e3fe5-33d1-4b55-bf1d-771fab5ed233');


INSERT INTO announcements
(id, announcement_status, announcement_type, content, created_date, modified_date, title,
 created_by, modified_by)
VALUES ('1bb7bb81-32de-4a32-9786-e50c518a0666', NULL, 'PAY_FEE', 'huan huan huan',
        '2025-10-27', NULL, 'test title',
        '4af76b85-6971-47b1-a4aa-0f83da58f2f5', NULL);


INSERT INTO notification_events
    (id, content, created_at, related_id, title, "type", created_by)
VALUES ('e7127daf-583d-4d83-9cc0-ab567cd622a7', 'huan huan huan', '2025-10-27 18:59:05.838629',
        '1bb7bb81-32de-4a32-9786-e50c518a0666', 'test title', 'ANNOUNCEMENT', '4af76b85-6971-47b1-a4aa-0f83da58f2f5');


INSERT INTO user_notifications
(id, delivered_at, is_read, notification_status, read_at, notification_event_id, user_id)
VALUES ('ff4ed179-99a5-4212-bf1a-b150e352ce7b', NULL, false, 'SENT', NULL, 'e7127daf-583d-4d83-9cc0-ab567cd622a7',
        'c264acd5-a212-4f97-9a85-e68da8b48e38');