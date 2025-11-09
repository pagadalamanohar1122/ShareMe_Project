package com.tasksphere.shareme.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request object for searching and filtering tasks")
public class TaskSearchRequest {
    
    @Schema(description = "Search query to filter tasks by title or description", example = "authentication")
    private String query;
    
    @Schema(description = "Filter tasks by status", example = "IN_PROGRESS", 
            allowableValues = {"TODO", "IN_PROGRESS", "COMPLETED", "CANCELLED"})
    private String status;
    
    @Schema(description = "Filter tasks by priority", example = "HIGH", 
            allowableValues = {"LOW", "MEDIUM", "HIGH", "URGENT"})
    private String priority;
    
    @Schema(description = "Filter tasks by project ID", example = "1")
    private Long projectId;
    
    @Schema(description = "Filter tasks by assignee ID", example = "2")
    private Long assigneeId;
    
    @Schema(description = "Filter tasks by creator ID", example = "1")
    private Long creatorId;
    
    @Schema(description = "Sort field for ordering results", example = "createdAt", 
            allowableValues = {"createdAt", "updatedAt", "dueDate", "title", "priority"})
    private String sortBy = "createdAt";
    
    @Schema(description = "Sort direction", example = "DESC", allowableValues = {"ASC", "DESC"})
    private String sortDirection = "DESC";
    
    @Schema(description = "Page number for pagination (0-based)", example = "0")
    private int page = 0;
    
    @Schema(description = "Number of items per page", example = "20")
    private int size = 20;
    
    // Constructors
    public TaskSearchRequest() {}
    
    public TaskSearchRequest(String query, String status, String priority, Long projectId, 
                           Long assigneeId, Long creatorId, String sortBy, String sortDirection, 
                           int page, int size) {
        this.query = query;
        this.status = status;
        this.priority = priority;
        this.projectId = projectId;
        this.assigneeId = assigneeId;
        this.creatorId = creatorId;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
        this.page = page;
        this.size = size;
    }
    
    // Getters and Setters
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
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
    
    public Long getCreatorId() {
        return creatorId;
    }
    
    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }
    
    public String getSortBy() {
        return sortBy;
    }
    
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
    
    public String getSortDirection() {
        return sortDirection;
    }
    
    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
    
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        this.page = page;
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
}