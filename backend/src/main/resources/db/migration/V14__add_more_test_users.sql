INSERT INTO `users` (`first_name`, `last_name`, `email`, `password_hash`, `role`, `created_at`) 
VALUES 
('Alice', 'Smith', 'alice.smith@example.com', '$2a$10$46bJgwnWp/haBjTJqecm/.TY69UVW9W3G7BZUFvB3xOlT7WQI14Tq', 'MEMBER', NOW()),
('Bob', 'Johnson', 'bob.johnson@example.com', '$2a$10$46bJgwnWp/haBjTJqecm/.TY69UVW9W3G7BZUFvB3xOlT7WQI14Tq', 'MEMBER', NOW()),
('Charlie', 'Brown', 'charlie.brown@example.com', '$2a$10$46bJgwnWp/haBjTJqecm/.TY69UVW9W3G7BZUFvB3xOlT7WQI14Tq', 'MEMBER', NOW());

-- All users have the same password: password123