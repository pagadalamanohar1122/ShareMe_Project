-- V4__fix_role_column_type.sql
-- Fix the role column type to ENUM

ALTER TABLE `users` MODIFY COLUMN `role` ENUM('MEMBER','ADMIN') NOT NULL DEFAULT 'MEMBER';