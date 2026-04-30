CREATE TABLE IF NOT EXISTS customer (
    user_id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255),
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
