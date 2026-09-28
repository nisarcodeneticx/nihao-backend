ALTER TABLE users
    ADD COLUMN google_id VARCHAR(64) NULL,
    ADD COLUMN auth_provider VARCHAR(20) NOT NULL DEFAULT 'PASSWORD';

CREATE UNIQUE INDEX uk_users_google_id ON users (google_id);
