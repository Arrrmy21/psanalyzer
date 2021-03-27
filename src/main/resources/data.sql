INSERT INTO user_roles (id, name) VALUES (1, 'ROLE_ADMIN')
ON CONFLICT (id) DO UPDATE
SET name = 'ROLE_ADMIN';
INSERT INTO user_roles (id, name) VALUES (2, 'ROLE_USER')
ON CONFLICT (id) DO UPDATE
SET name = 'ROLE_USER';

create table IF NOT EXISTS games_history (
	game_id VARCHAR (50) NOT NULL,
	game_price integer NOT NULL,
	psplus_price integer NOT NULL,
	date_of_change TIMESTAMP NOT NULL,
	is_available boolean NOT NULL
)