package com.tasksphere.shareme.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tasksphere.shareme.dto.ErrorResponse;
import com.tasksphere.shareme.dto.TaskRequest;
import com.tasksphere.shareme.dto.TaskResponse;
import com.tasksphere.shareme.dto.TaskSearchRequest;
import com.tasksphere.shareme.security.JwtTokenProvider;
import com.tasksphere.shareme.service.TaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"})
@Tag(name = "Tasks", description = "Search, create, update, status, stats for tasks")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class TaskController {
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @GetMapping
    @Operation(summary = "Get Tasks with Search and Filtering", 
              description = "Retrieve tasks accessible by the user with advanced search, filtering, and pagination capabilities")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully",
                content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<TaskResponse>> getTasks(
            @Parameter(description = "Search query for task title or description", example = "authentication")
            @RequestParam(required = false) String query,
            
            @Parameter(description = "Filter by task status", example = "IN_PROGRESS")
            @RequestParam(required = false) String status,
            
            @Parameter(description = "Filter by task priority", example = "HIGH")
            @RequestParam(required = false) String priority,
            
            @Parameter(description = "Filter by project ID", example = "1")
            @RequestParam(required = false) Long projectId,
            
            @Parameter(description = "Filter by assignee ID", example = "2")
            @RequestParam(required = false) Long assigneeId,
            
            @Parameter(description = "Filter by creator ID", example = "1")
            @RequestParam(required = false) Long creatorId,
            
            @Parameter(description = "Sort field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "Sort direction", example = "DESC")
            @RequestParam(defaultValue = "DESC") String sortDirection,
            
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            
            @Parameter(description = "JWT Bearer token for authentication", required = true)
            @RequestHeader("Authorization") String token) {
        
        try {
            String jwt = token.substring(7); // Remove "Bearer " prefix
            Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
            
            TaskSearchRequest searchRequest = new TaskSearchRequest(
                query, status, priority, projectId, assigneeId, creatorId,
                sortBy, sortDirection, page, size
            );
            
            Page<TaskResponse> tasks = taskService.getUserAccessibleTasks(userId, searchRequest);
            return ResponseEntity.ok(tasks);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get Task by ID", description = "Retrieve a specific task by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task retrieved successfully",
                content = @Content(schema = @Schema(implementation = TaskResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden - Access denied to this task",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Task not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TaskResponse> getTask(
            @Parameter(description = "Task ID to retrieve", required = true, example = "1")
            @PathVariable Long id,
            
            @Parameter(description = "JWT Bearer token for authentication", required = true)
            @RequestHeader("Authorization") String token) {
        
        try {
            String jwt = token.substring(7); // Remove "Bearer " prefix
            Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
            
            TaskResponse task = taskService.getTaskById(id, userId);
            return ResponseEntity.ok(task);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping
    @Operation(summary = "Create New Task", description = "Create a new task in a project")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Task created successfully",
                content = @Content(schema = @Schema(implementation = TaskResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad request - Invalid task data",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden - No permission to create task in this project",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Project or assignee not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TaskResponse> createTask(
            @Parameter(description = "Task creation data", required = true)
            @Valid @RequestBody TaskRequest taskRequest,
            
            @Parameter(description = "JWT Bearer token for authentication", required = true)
            @RequestHeader("Authorization") String token) {
        
        try {
            String jwt = token.substring(7); // Remove "Bearer " prefix
            Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
            
            TaskResponse createdTask = taskService.createTask(taskRequest, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update Task", description = "Update an existing task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task updated successfully",
                content = @Content(schema = @Schema(implementation = TaskResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad request - Invalid task data",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden - No permission to update this task",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Task not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TaskResponse> updateTask(
            @Parameter(description = "Task ID to update", required = true, example = "1")
            @PathVariable Long id,
            
            @Parameter(description = "Updated task data", required = true)
            @Valid @RequestBody TaskRequest taskRequest,
            
            @Parameter(description = "JWT Bearer token for authentication", required = true)
            @RequestHeader("Authorization") String token) {
        
        try {
            String jwt = token.substring(7); // Remove "Bearer " prefix
            Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
            
            TaskResponse updatedTask = taskService.updateTask(id, taskRequest, userId);
            return ResponseEntity.ok(updatedTask);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update Task Status", description = "Update only the status of a task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task status updated successfully",
                content = @Content(schema = @Schema(implementation = TaskResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad request - Invalid status",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden - No access to this task",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Task not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @Parameter(description = "Task ID to update", required = true, example = "1")
            @PathVariable Long id,
            
            @Parameter(description = "New task status", required = true)
            @RequestBody Map<String, String> statusUpdate,
            
            @Parameter(description = "JWT Bearer token for authentication", required = true)
            @RequestHeader("Authorization") String token) {
        
        try {
            String jwt = token.substring(7); // Remove "Bearer " prefix
            Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
            
            String status = statusUpdate.get("status");
            if (status == null || status.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            TaskResponse updatedTask = taskService.updateTaskStatus(id, status, userId);
            return ResponseEntity.ok(updatedTask);
            
        } catch (RuntimeException e) {
            // Handle specific exceptions
            if (e.getMessage().contains("Task not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("access") || e.getMessage().contains("permission")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } else if (e.getMessage().contains("Invalid task status")) {
                return ResponseEntity.badRequest().build();
            }
            System.err.println("Error updating task status: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            System.err.println("Unexpected error updating task status: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Task", description = "Delete a task permanently")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - No permission to delete this task",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Task not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "Task ID to delete", required = true, example = "1")
            @PathVariable Long id,
            
            @Parameter(description = "JWT Bearer token for authentication", required = true)
            @RequestHeader("Authorization") String token) {
        
        try {
            String jwt = token.substring(7); // Remove "Bearer " prefix
            Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
            
            taskService.deleteTask(id, userId);
            return ResponseEntity.noContent().build();
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/stats")
    @Operation(summary = "Get Task Statistics", description = "Get task statistics for the current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Map<String, Object>> getTaskStats(
            @Parameter(description = "JWT Bearer token for authentication", required = true)
            @RequestHeader("Authorization") String token) {
        
        try {
            String jwt = token.substring(7); // Remove "Bearer " prefix
            Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
            
            // Get quick stats for dashboard
            TaskSearchRequest allTasks = new TaskSearchRequest();
            allTasks.setSize(1); // We only need count
            
            TaskSearchRequest todoTasks = new TaskSearchRequest();
            todoTasks.setStatus("TODO");
            todoTasks.setSize(1);
            
            TaskSearchRequest inProgressTasks = new TaskSearchRequest();
            inProgressTasks.setStatus("IN_PROGRESS");
            inProgressTasks.setSize(1);
            
            TaskSearchRequest completedTasks = new TaskSearchRequest();
            completedTasks.setStatus("COMPLETED");
            completedTasks.setSize(1);
            
            Page<TaskResponse> all = taskService.getUserAccessibleTasks(userId, allTasks);
            Page<TaskResponse> todo = taskService.getUserAccessibleTasks(userId, todoTasks);
            Page<TaskResponse> inProgress = taskService.getUserAccessibleTasks(userId, inProgressTasks);
            Page<TaskResponse> completed = taskService.getUserAccessibleTasks(userId, completedTasks);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalTasks", all.getTotalElements());
            stats.put("todoTasks", todo.getTotalElements());
            stats.put("inProgressTasks", inProgress.getTotalElements());
            stats.put("completedTasks", completed.getTotalElements());
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}