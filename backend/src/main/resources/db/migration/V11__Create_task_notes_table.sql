-- Create task_notes table for personal user notes on tasks
CREATE TABLE task_notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    note_content TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_task_note (user_id, task_id)
);

-- Create task_note_tags table for reminder tags
CREATE TABLE task_note_tags (
    task_note_id BIGINT NOT NULL,
    tag VARCHAR(50) NOT NULL,
    FOREIGN KEY (task_note_id) REFERENCES task_notes(id) ON DELETE CASCADE,
    INDEX idx_task_note_tags (task_note_id),
    INDEX idx_tag (tag)
);

-- Add indexes for better performance
CREATE INDEX idx_task_notes_user_updated ON task_notes(user_id, updated_at DESC);
CREATE INDEX idx_task_notes_task_updated ON task_notes(task_id, updated_at DESC);
CREATE INDEX idx_task_notes_user_created ON task_notes(user_id, created_at DESC);