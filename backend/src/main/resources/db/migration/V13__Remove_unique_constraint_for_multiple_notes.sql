-- V13: Remove unique constraint to allow multiple notes per task
-- This allows users to create multiple personal notes for the same task

-- Drop the unique constraint that was preventing multiple notes per user-task combination
ALTER TABLE task_notes DROP INDEX unique_user_task_note;

-- Also allow NULL task_id for standalone notes (notes not linked to any task)
ALTER TABLE task_notes MODIFY COLUMN task_id BIGINT NULL;

-- Update the foreign key constraint to handle NULL task_id
ALTER TABLE task_notes DROP FOREIGN KEY task_notes_ibfk_2;
ALTER TABLE task_notes ADD CONSTRAINT fk_task_notes_task_id 
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE;