-- V2__insert_dummy_user.sql
-- Insert a dummy user for testing

INSERT INTO `users` (`first_name`, `last_name`, `email`, `password_hash`, `role`) 
VALUES ('John', 'Doe', 'john.doe@example.com', '$2a$10$46bJgwnWp/haBjTJqecm/.TY69UVW9W3G7BZUFvB3xOlT7WQI14Tq', 'MEMBER');

-- Password is: password123
-- This is the BCrypt hash for 'password123'