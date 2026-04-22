CREATE DATABASE governance_service_db;
CREATE DATABASE audit_service_db;
CREATE DATABASE user_service_db;

\c user_service_db;

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL
);

INSERT INTO users (name, username, email, password, role)
VALUES ('Admin', 'admin', 'admin@example.com', 'admin123', 'admin');