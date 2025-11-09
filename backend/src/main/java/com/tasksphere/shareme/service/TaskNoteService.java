package com.tasksphere.shareme.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tasksphere.shareme.dto.TaskNoteRequest;
import com.tasksphere.shareme.dto.TaskNoteResponse;
import com.tasksphere.shareme.entity.Task;
import com.tasksphere.shareme.entity.TaskNote;
import com.tasksphere.shareme.entity.User;
import com.tasksphere.shareme.exception.ResourceNotFoundException;
import com.tasksphere.shareme.repository.TaskNoteRepository;
import com.tasksphere.shareme.repository.TaskRepository;
import com.tasksphere.shareme.repository.UserRepository;

@Service
@Transactional
public class TaskNoteService {
    
    private static final Logger logger = LoggerFactory.getLogger(TaskNoteService.class);
    
    @Autowired
    private TaskNoteRepository taskNoteRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get or create a task note for a user and task
     */
    public TaskNoteResponse getTaskNote(Long userId, Long taskId) {
        logger.debug("Getting task note for user {} and task {}", userId, taskId);
        
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        if (taskId == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }
        
        // Verify user exists
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Verify task exists
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        
        // Get existing note or return empty response
        Optional<TaskNote> taskNoteOpt = taskNoteRepository.findByUserIdAndTaskId(userId, taskId);
        
        if (taskNoteOpt.isPresent()) {
            return convertToResponse(taskNoteOpt.get());
        } else {
            // Return empty note response
            return new TaskNoteResponse(null, taskId, task.getTitle(), null, "", 
                                      List.of(), null, null);
        }
    }
    
    /**
     * Create or update a task note
     */
    public TaskNoteResponse saveTaskNote(Long userId, TaskNoteRequest request) {
        logger.debug("Saving task note for user {} and task {}", userId, request.getTaskId());
        
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        if (request == null) {
            throw new IllegalArgumentException("Task note request cannot be null");
        }
        
        // Verify user exists
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        Task task = null;
        
        // Verify task exists if taskId is provided
        if (request.getTaskId() != null) {
            task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + request.getTaskId()));
        }
        
        // Always create a new note (allow multiple notes per task)
        TaskNote taskNote = new TaskNote(user, task, request.getNoteName(), request.getNoteContent(), request.getReminderTags());
        
        TaskNote savedNote = taskNoteRepository.save(taskNote);
        logger.info("Task note saved successfully for user {} and task {}", userId, request.getTaskId());
        
        return convertToResponse(savedNote);
    }
    
    /**
     * Delete a task note
     */
    public void deleteTaskNote(Long userId, Long taskId) {
        logger.debug("Deleting task note for user {} and task {}", userId, taskId);
        
        // Verify the note exists and belongs to the user
        TaskNote taskNote = taskNoteRepository.findByUserIdAndTaskId(userId, taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task note not found for user " + userId + " and task " + taskId));
        
        taskNoteRepository.delete(taskNote);
        logger.info("Task note deleted successfully for user {} and task {}", userId, taskId);
    }
    
    /**
     * Get all task notes for a user
     */
    @Transactional(readOnly = true)
    public List<TaskNoteResponse> getUserTaskNotes(Long userId) {
        logger.debug("Getting all task notes for user {}", userId);
        
        List<TaskNote> taskNotes = taskNoteRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        return taskNotes.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get task notes by tag for a user
     */
    @Transactional(readOnly = true)
    public List<TaskNoteResponse> getUserTaskNotesByTag(Long userId, String tag) {
        logger.debug("Getting task notes for user {} with tag {}", userId, tag);
        
        List<TaskNote> taskNotes = taskNoteRepository.findByUserIdAndReminderTagsContaining(userId, tag);
        return taskNotes.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all unique tags used by a user
     */
    @Transactional(readOnly = true)
    public List<String> getUserTags(Long userId) {
        logger.debug("Getting all tags for user {}", userId);
        return taskNoteRepository.findDistinctTagsByUserId(userId);
    }
    
    /**
     * Check if user has a note for a task
     */
    @Transactional(readOnly = true)
    public boolean hasTaskNote(Long userId, Long taskId) {
        if (userId == null || taskId == null) {
            throw new IllegalArgumentException("User ID and Task ID cannot be null");
        }

        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        // Verify task exists
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found with id: " + taskId);
        }

        // Check if note exists
        return taskNoteRepository.existsByUserIdAndTaskId(userId, taskId);
    }
    
    /**
     * Convert TaskNote entity to TaskNoteResponse DTO
     */
    private TaskNoteResponse convertToResponse(TaskNote taskNote) {
        Long taskId = taskNote.getTask() != null ? taskNote.getTask().getId() : null;
        String taskTitle = taskNote.getTask() != null ? taskNote.getTask().getTitle() : null;
        
        return new TaskNoteResponse(
            taskNote.getId(),
            taskId,
            taskTitle,
            taskNote.getNoteName(),
            taskNote.getNoteContent(),
            taskNote.getReminderTags(),
            taskNote.getCreatedAt(),
            taskNote.getUpdatedAt()
        );
    }
}