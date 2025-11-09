package com.tasksphere.shareme.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TaskNoteResponse {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("taskId")
    private Long taskId;
    
    @JsonProperty("taskTitle")
    private String taskTitle;
    
    @JsonProperty("noteName")
    private String noteName;
    
    @JsonProperty("noteContent")
    private String noteContent;
    
    @JsonProperty("reminderTags")
    private List<String> reminderTags;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    // Constructors
    public TaskNoteResponse() {}
    
    public TaskNoteResponse(Long id, Long taskId, String taskTitle, String noteName, String noteContent, 
                           List<String> reminderTags, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.noteName = noteName;
        this.noteContent = noteContent;
        this.reminderTags = reminderTags;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public TaskNoteResponse(Long id, Long taskId, String taskTitle, String noteContent, 
                           List<String> reminderTags, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.noteContent = noteContent;
        this.reminderTags = reminderTags;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getTaskId() {
        return taskId;
    }
    
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
    
    public String getTaskTitle() {
        return taskTitle;
    }
    
    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
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
}