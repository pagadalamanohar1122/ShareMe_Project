package com.tasksphere.shareme.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "task_notes")
public class TaskNote {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = true)
    private Task task;
    
    @Column(name = "note_name", length = 255)
    @Size(max = 255, message = "Note name cannot exceed 255 characters")
    private String noteName;
    
    @Lob
    @Column(name = "note_content", columnDefinition = "TEXT")
    private String noteContent;
    
    @ElementCollection
    @CollectionTable(name = "task_note_tags", joinColumns = @JoinColumn(name = "task_note_id"))
    @Column(name = "tag")
    @Size(max = 50, message = "Tag cannot exceed 50 characters")
    private List<String> reminderTags;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public TaskNote() {}
    
    public TaskNote(User user, Task task, String noteName, String noteContent, List<String> reminderTags) {
        this.user = user;
        this.task = task; // Can be null for standalone notes
        this.noteName = noteName;
        this.noteContent = noteContent;
        this.reminderTags = reminderTags;
    }
    
    public TaskNote(User user, Task task, String noteContent, List<String> reminderTags) {
        this.user = user;
        this.task = task; // Can be null for standalone notes
        this.noteContent = noteContent;
        this.reminderTags = reminderTags;
    }
    
    // Constructor for standalone notes (without task)
    public TaskNote(User user, String noteName, String noteContent, List<String> reminderTags) {
        this.user = user;
        this.task = null; // Standalone note
        this.noteName = noteName;
        this.noteContent = noteContent;
        this.reminderTags = reminderTags;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Task getTask() {
        return task;
    }
    
    public void setTask(Task task) {
        this.task = task;
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
    
    @Override
    public String toString() {
        return "TaskNote{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", taskId=" + (task != null ? task.getId() : null) +
                ", noteContent='" + noteContent + '\'' +
                ", reminderTags=" + reminderTags +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}