-- V3__fix_token_hash_column_type.sql
-- Fix the token_hash column type to VARCHAR

ALTER TABLE `password_resets` MODIFY COLUMN `token_hash` VARCHAR(64) NOT NULL;