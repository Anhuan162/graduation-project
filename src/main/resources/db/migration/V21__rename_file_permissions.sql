ALTER TABLE roles_permissions
DROP CONSTRAINT IF EXISTS roles_permissions_permissions_name_fkey;

ALTER TABLE roles_permissions
ADD CONSTRAINT roles_permissions_permissions_name_fkey
FOREIGN KEY (permissions_name)
REFERENCES permissions(name)
ON UPDATE CASCADE
ON DELETE RESTRICT;

UPDATE permissions
SET name = 'CREATE_OWN_FILE'
WHERE name = 'CREATE_ANY_FILES';

UPDATE permissions
SET name = 'MANAGE_ALL_FILES'
WHERE name = 'CREATE_ALL_FILES';

INSERT INTO roles_permissions (role_name, permissions_name)
SELECT 'USER', 'CREATE_OWN_FILE'
WHERE EXISTS (SELECT 1 FROM permissions WHERE name = 'CREATE_OWN_FILE')
ON CONFLICT DO NOTHING;
