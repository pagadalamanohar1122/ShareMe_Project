package com.tasksphere.shareme.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request object for creating or updating a task")
public class TaskRequest {
    
    @NotBlank(message = "Task title is required")
    @Size(min = 3, max = 200, message = "Task title must be between 3 and 200 characters")
    @Schema(description = "Title of the task", example = "Implement user authentication", required = true)
    private String title;
    
    @Schema(description = "Detailed description of the task", example = "Implement JWT-based authentication system with login and registration")
    private String description;
    
    @Schema(description = "Status of the task", example = "TODO", allowableValues = {"TODO", "IN_PROGRESS", "COMPLETED", "CANCELLED"})
    private String status = "TODO";
    
    @Schema(description = "Priority level of the task", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH", "URGENT"})
    private String priority = "MEDIUM";
    
    @NotNull(message = "Project ID is required")
    @Schema(description = "ID of the project this task belongs to", example = "1", required = true)
    private Long projectId;
    
    @Schema(description = "ID of the user assigned to this task", example = "2")
    private Long assigneeId;
    
    @Schema(description = "Due date for the task", example = "2024-12-31T23:59:59")
    private LocalDateTime dueDate;
    
    // Constructors
    public TaskRequest() {}
    
    public TaskRequest(String title, String description, String status, String priority, 
                      Long projectId, Long assigneeId, LocalDateTime dueDate) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.projectId = projectId;
        this.assigneeId = assigneeId;
        this.dueDate = dueDate;
    }
    
    // Getters and Setters
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
    
    public Long getProjectId() {
        return projectId;
    }
    
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
    
    public Long getAssigneeId() {
        return assigneeId;
    }
    
    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }
    
    public LocalDateTime getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
}