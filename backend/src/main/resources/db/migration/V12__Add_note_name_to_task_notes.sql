-- V12: Add note_name column to task_notes table
ALTER TABLE task_notes ADD COLUMN note_name VARCHAR(255) AFTER task_id;