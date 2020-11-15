INSERT INTO user_roles (id, name) SELECT 1, 'ROLE_ADMIN' FROM user_roles
WHERE NOT EXISTS (SELECT * FROM user_roles WHERE id = '1' AND name = 'ROLE_ADMIN');
INSERT INTO user_roles (id, name) SELECT 2, 'ROLE_USER' FROM user_roles
WHERE NOT EXISTS (SELECT * FROM user_roles WHERE id = '2' AND name = 'ROLE_USER');