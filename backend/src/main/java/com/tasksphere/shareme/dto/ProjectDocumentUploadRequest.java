package com.tasksphere.shareme.dto;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;

public class ProjectDocumentUploadRequest {
    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotNull(message = "Document file is required")
    private MultipartFile document;

    // Getters and Setters
    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public MultipartFile getDocument() {
        return document;
    }

    public void setDocument(MultipartFile document) {
        this.document = document;
    }
}