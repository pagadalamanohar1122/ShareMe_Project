package com.tasksphere.shareme.dto;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response object containing task details")
public class TaskResponse {
    
    @Schema(description = "Unique identifier of the task", example = "1")
    private Long id;
    
    @Schema(description = "Title of the task", example = "Implement user authentication")
    private String title;
    
    @Schema(description = "Detailed description of the task", example = "Implement JWT-based authentication system")
    private String description;
    
    @Schema(description = "Current status of the task", example = "IN_PROGRESS")
    private String status;
    
    @Schema(description = "Priority level of the task", example = "HIGH")
    private String priority;
    
    @Schema(description = "Project information this task belongs to")
    private ProjectInfo project;
    
    @Schema(description = "User assigned to this task")
    private UserInfo assignee;
    
    @Schema(description = "User who created this task")
    private UserInfo creator;
    
    @Schema(description = "When the task was created", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "When the task was last updated", example = "2024-01-16T14:20:00")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Due date for the task", example = "2024-12-31T23:59:59")
    private LocalDateTime dueDate;
    
    @Schema(description = "List of attachments for this task")
    private List<TaskAttachmentResponse> attachments;
    
    // Constructors
    public TaskResponse() {}
    
    public TaskResponse(Long id, String title, String description, String status, String priority,
                       ProjectInfo project, UserInfo assignee, UserInfo creator,
                       LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime dueDate,
                       List<TaskAttachmentResponse> attachments) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.project = project;
        this.assignee = assignee;
        this.creator = creator;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.dueDate = dueDate;
        this.attachments = attachments;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public ProjectInfo getProject() {
        return project;
    }
    
    public void setProject(ProjectInfo project) {
        this.project = project;
    }
    
    public UserInfo getAssignee() {
        return assignee;
    }
    
    public void setAssignee(UserInfo assignee) {
        this.assignee = assignee;
    }
    
    public UserInfo getCreator() {
        return creator;
    }
    
    public void setCreator(UserInfo creator) {
        this.creator = creator;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
    
    public List<TaskAttachmentResponse> getAttachments() {
        return attachments;
    }
    
    public void setAttachments(List<TaskAttachmentResponse> attachments) {
        this.attachments = attachments;
    }
    
    // Inner classes for nested objects
    @Schema(description = "Project information")
    public static class ProjectInfo {
        @Schema(description = "Project ID", example = "1")
        private Long id;
        
        @Schema(description = "Project name", example = "ShareMe TaskSphere")
        private String name;
        
        @Schema(description = "Project status", example = "ACTIVE")
        private String status;
        
        public ProjectInfo() {}
        
        public ProjectInfo(Long id, String name, String status) {
            this.id = id;
            this.name = name;
            this.status = status;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}