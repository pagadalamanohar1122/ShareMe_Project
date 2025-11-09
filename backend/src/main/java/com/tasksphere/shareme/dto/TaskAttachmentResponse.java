package com.tasksphere.shareme.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response object containing task attachment details")
public class TaskAttachmentResponse {
    
    @Schema(description = "Unique identifier of the attachment", example = "1")
    private Long id;
    
    @Schema(description = "Original filename when uploaded", example = "project_requirements.pdf")
    private String originalFilename;
    
    @Schema(description = "File size in bytes", example = "2048576")
    private Long fileSize;
    
    @Schema(description = "MIME content type", example = "application/pdf")
    private String contentType;
    
    @Schema(description = "User who uploaded the file")
    private UserInfo uploadedBy;
    
    @Schema(description = "When the file was uploaded", example = "2024-01-15T10:30:00")
    private LocalDateTime uploadedAt;
    
    @Schema(description = "Download URL for the file", example = "/api/tasks/1/attachments/1/download")
    private String downloadUrl;
    
    // Constructors
    public TaskAttachmentResponse() {}
    
    public TaskAttachmentResponse(Long id, String originalFilename, Long fileSize, String contentType,
                                 UserInfo uploadedBy, LocalDateTime uploadedAt, String downloadUrl) {
        this.id = id;
        this.originalFilename = originalFilename;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = uploadedAt;
        this.downloadUrl = downloadUrl;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getOriginalFilename() {
        return originalFilename;
    }
    
    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public UserInfo getUploadedBy() {
        return uploadedBy;
    }
    
    public void setUploadedBy(UserInfo uploadedBy) {
        this.uploadedBy = uploadedBy;
    }
    
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
    
    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
    
    public String getDownloadUrl() {
        return downloadUrl;
    }
    
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}