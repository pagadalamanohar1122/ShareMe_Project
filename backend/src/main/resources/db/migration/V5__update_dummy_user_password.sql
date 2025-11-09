-- V5__update_dummy_user_password.sql
-- Update the dummy user with correct password hash

UPDATE `users` 
SET `password_hash` = '$2a$10$46bJgwnWp/haBjTJqecm/.TY69UVW9W3G7BZUFvB3xOlT7WQI14Tq'
WHERE `email` = 'john.doe@example.com';