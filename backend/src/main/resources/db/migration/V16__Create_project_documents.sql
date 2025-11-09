CREATE TABLE project_documents (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    project_id BIGINT NOT NULL,
    uploaded_at TIMESTAMP NOT NULL,
    uploaded_by BIGINT NOT NULL,
    FOREIGN KEY (project_id) REFERENCES projects(id),
    FOREIGN KEY (uploaded_by) REFERENCES users(id)
);