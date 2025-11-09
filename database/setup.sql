-- Database setup script for ShareMe TaskSphere

-- Create database
CREATE DATABASE IF NOT EXISTS shareme CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user (modify password as needed for production)
CREATE USER IF NOT EXISTS 'shareme'@'localhost' IDENTIFIED BY 'ShareMe@123';

-- Grant privileges
GRANT ALL PRIVILEGES ON shareme.* TO 'shareme'@'localhost';

-- Apply changes
FLUSH PRIVILEGES;

-- Switch to the database
USE shareme;

-- Verify setup
SELECT 'Database setup completed successfully!' as status;