package com.tasksphere.shareme.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tasksphere.shareme.dto.TaskRequest;
import com.tasksphere.shareme.dto.TaskResponse;
import com.tasksphere.shareme.dto.TaskSearchRequest;
import com.tasksphere.shareme.dto.UserInfo;
import com.tasksphere.shareme.entity.Project;
import com.tasksphere.shareme.entity.Task;
import com.tasksphere.shareme.entity.User;
import com.tasksphere.shareme.exception.ResourceNotFoundException;
import com.tasksphere.shareme.exception.UnauthorizedException;
import com.tasksphere.shareme.repository.ProjectRepository;
import com.tasksphere.shareme.repository.TaskRepository;
import com.tasksphere.shareme.repository.UserRepository;

@Service
@Transactional
public class TaskService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TaskAttachmentService taskAttachmentService;
    
    /**
     * Get all tasks accessible by the user with search and filtering
     */
    public Page<TaskResponse> getUserAccessibleTasks(Long userId, TaskSearchRequest searchRequest) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Create pageable with sorting
        Sort sort = createSort(searchRequest.getSortBy(), searchRequest.getSortDirection());
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);
        
        // Convert string status/priority to enums
        Task.TaskStatus status = parseTaskStatus(searchRequest.getStatus());
        Task.TaskPriority priority = parseTaskPriority(searchRequest.getPriority());
        
        Page<Task> tasks = taskRepository.findUserAccessibleTasksWithFilters(
            userId,
            searchRequest.getQuery(),
            status,
            priority,
            searchRequest.getProjectId(),
            pageable
        );
        
        return tasks.map(this::convertToTaskResponse);
    }
    
    /**
     * Get task by ID (with access control)
     */
    public TaskResponse getTaskById(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        
        // Check if user has access to this task
        if (!hasTaskAccess(task, userId)) {
            throw new UnauthorizedException("You don't have access to this task");
        }
        
        return convertToTaskResponse(task);
    }
    
    /**
     * Create a new task
     */
    public TaskResponse createTask(TaskRequest taskRequest, Long userId) {
        // Validate project exists and user has access
        Project project = projectRepository.findById(taskRequest.getProjectId())
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + taskRequest.getProjectId()));
        
        User creator = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Check if user has access to the project
        if (!project.getOwner().getId().equals(userId)) {
            // Add more permission logic here if needed (e.g., project members)
            throw new UnauthorizedException("You don't have permission to create tasks in this project");
        }
        
        // Create task
        Task task = new Task();
        task.setTitle(taskRequest.getTitle());
        task.setDescription(taskRequest.getDescription());
        task.setStatus(parseTaskStatus(taskRequest.getStatus()));
        task.setPriority(parseTaskPriority(taskRequest.getPriority()));
        task.setProject(project);
        task.setCreator(creator);
        task.setDueDate(taskRequest.getDueDate());
        
        // Set assignee if provided
        if (taskRequest.getAssigneeId() != null) {
            User assignee = userRepository.findById(taskRequest.getAssigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id: " + taskRequest.getAssigneeId()));
            task.setAssignee(assignee);
        }
        
        Task savedTask = taskRepository.save(task);
        return convertToTaskResponse(savedTask);
    }
    
    /**
     * Update an existing task
     */
    public TaskResponse updateTask(Long taskId, TaskRequest taskRequest, Long userId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        
        // Check if user has permission to update this task
        if (!canModifyTask(task, userId)) {
            throw new UnauthorizedException("You don't have permission to update this task");
        }
        
        // Update task fields
        task.setTitle(taskRequest.getTitle());
        task.setDescription(taskRequest.getDescription());
        task.setStatus(parseTaskStatus(taskRequest.getStatus()));
        task.setPriority(parseTaskPriority(taskRequest.getPriority()));
        task.setDueDate(taskRequest.getDueDate());
        
        // Update assignee if provided
        if (taskRequest.getAssigneeId() != null) {
            User assignee = userRepository.findById(taskRequest.getAssigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id: " + taskRequest.getAssigneeId()));
            task.setAssignee(assignee);
        } else {
            task.setAssignee(null);
        }
        
        Task updatedTask = taskRepository.save(task);
        return convertToTaskResponse(updatedTask);
    }
    
    /**
     * Delete a task
     */
    public void deleteTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        
        // Check if user has permission to delete this task
        if (!canModifyTask(task, userId)) {
            throw new UnauthorizedException("You don't have permission to delete this task");
        }
        
        taskRepository.delete(task);
    }
    
    /**
     * Update task status only
     */
    public TaskResponse updateTaskStatus(Long taskId, String status, Long userId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        
        // Check if user has access to this task
        if (!hasTaskAccess(task, userId)) {
            throw new UnauthorizedException("You don't have access to this task");
        }
        
        task.setStatus(parseTaskStatus(status));
        Task updatedTask = taskRepository.save(task);
        
        return convertToTaskResponse(updatedTask);
    }
    
    // Helper methods
    private boolean hasTaskAccess(Task task, Long userId) {
        return task.getCreator().getId().equals(userId) ||
               (task.getAssignee() != null && task.getAssignee().getId().equals(userId)) ||
               task.getProject().getOwner().getId().equals(userId);
    }
    
    /**
     * Check if a user has access to a task
     */
    public boolean userHasAccessToTask(Long userId, Long taskId) {
        try {
            Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
            return hasTaskAccess(task, userId);
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean canModifyTask(Task task, Long userId) {
        return task.getCreator().getId().equals(userId) ||
               task.getProject().getOwner().getId().equals(userId);
    }
    
    private Sort createSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) ? 
            Sort.Direction.ASC : Sort.Direction.DESC;
        
        switch (sortBy.toLowerCase()) {
            case "title":
                return Sort.by(direction, "title");
            case "status":
                return Sort.by(direction, "status");
            case "priority":
                return Sort.by(direction, "priority");
            case "duedate":
                return Sort.by(direction, "dueDate");
            case "updatedat":
                return Sort.by(direction, "updatedAt");
            default:
                return Sort.by(direction, "createdAt");
        }
    }
    
    private Task.TaskStatus parseTaskStatus(String status) {
        if (status == null) return null;
        try {
            return Task.TaskStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid task status: " + status);
        }
    }
    
    private Task.TaskPriority parseTaskPriority(String priority) {
        if (priority == null) return null;
        try {
            return Task.TaskPriority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid task priority: " + priority);
        }
    }
    
    private TaskResponse convertToTaskResponse(Task task) {
        TaskResponse.ProjectInfo projectInfo = new TaskResponse.ProjectInfo(
            task.getProject().getId(),
            task.getProject().getName(),
            task.getProject().getStatus().toString()
        );
        
        UserInfo creatorInfo = new UserInfo(
            task.getCreator().getId(),
            task.getCreator().getFirstName(),
            task.getCreator().getLastName(),
            task.getCreator().getEmail(),
            task.getCreator().getRole().toString()
        );
        
        UserInfo assigneeInfo = null;
        if (task.getAssignee() != null) {
            assigneeInfo = new UserInfo(
                task.getAssignee().getId(),
                task.getAssignee().getFirstName(),
                task.getAssignee().getLastName(),
                task.getAssignee().getEmail(),
                task.getAssignee().getRole().toString()
            );
        }
        
        // Get attachments for this task
        var attachments = taskAttachmentService.getTaskAttachments(task.getId());
        
        return new TaskResponse(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus().toString(),
            task.getPriority().toString(),
            projectInfo,
            assigneeInfo,
            creatorInfo,
            task.getCreatedAt(),
            task.getUpdatedAt(),
            task.getDueDate(),
            attachments
        );
    }
}