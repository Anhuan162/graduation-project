INSERT INTO permissions (name, resource_type, permission_type)
VALUES
    ('CREATE_ANY_FILES', 'FILE', 'create_any'),
    ('CREATE_ALL_FILES', 'FILE', 'create_all');

INSERT INTO roles_permissions (role_name, permissions_name)
VALUES
    ('ADMIN', 'CREATE_ANY_FILES'),
    ('ADMIN', 'CREATE_ALL_FILES');
