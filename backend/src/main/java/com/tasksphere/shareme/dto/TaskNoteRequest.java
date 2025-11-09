package com.tasksphere.shareme.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request object for creating or updating a personal task note")
public class TaskNoteRequest {
    
    @NotNull(message = "Task ID is required")
    @JsonProperty("taskId")
    @Schema(description = "ID of the task this note belongs to", example = "1", required = true)
    private Long taskId;
    
    @Size(max = 255, message = "Note name cannot exceed 255 characters")
    @JsonProperty("noteName")
    @Schema(description = "Title or name for the personal note", example = "Implementation Notes", maxLength = 255)
    private String noteName;
    
    @Size(max = 5000, message = "Note content cannot exceed 5000 characters")
    @JsonProperty("noteContent")
    @Schema(description = "Content of the personal note", 
            example = "Remember to implement proper error handling and add unit tests for the authentication flow", 
            maxLength = 5000)
    private String noteContent;
    
    @JsonProperty("reminderTags")
    @Schema(description = "List of reminder tags for categorization and quick filtering", 
            example = "[\"urgent\", \"review\", \"follow-up\"]")
    private List<@Size(max = 50, message = "Tag cannot exceed 50 characters") String> reminderTags;
    
    // Constructors
    public TaskNoteRequest() {}
    
    public TaskNoteRequest(Long taskId, String noteName, String noteContent, List<String> reminderTags) {
        this.taskId = taskId;
        this.noteName = noteName;
        this.noteContent = noteContent;
        this.reminderTags = reminderTags;
    }
    
    public TaskNoteRequest(Long taskId, String noteContent, List<String> reminderTags) {
        this.taskId = taskId;
        this.noteContent = noteContent;
        this.reminderTags = reminderTags;
    }
    
    // Getters and Setters
    public Long getTaskId() {
        return taskId;
    }
    
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
    
    public String getNoteName() {
        return noteName;
    }
    
    public void setNoteName(String noteName) {
        this.noteName = noteName;
    }
    
    public String getNoteContent() {
        return noteContent;
    }
    
    public void setNoteContent(String noteContent) {
        this.noteContent = noteContent;
    }
    
    public List<String> getReminderTags() {
        return reminderTags;
    }
    
    public void setReminderTags(List<String> reminderTags) {
        this.reminderTags = reminderTags;
    }
}