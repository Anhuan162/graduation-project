INSERT INTO permissions (name, resource_type, permission_type)
VALUES
    ('DELETE_ALL_PERMISSIONS', 'PERMISSION', 'delete_any')
ON CONFLICT DO NOTHING;

INSERT INTO roles_permissions (role_name, permissions_name)
VALUES
    ('ADMIN', 'DELETE_ALL_PERMISSIONS')
ON CONFLICT DO NOTHING;
