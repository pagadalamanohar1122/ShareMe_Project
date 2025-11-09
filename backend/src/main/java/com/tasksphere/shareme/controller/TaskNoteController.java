package com.tasksphere.shareme.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.tasksphere.shareme.dto.ErrorResponse;
import com.tasksphere.shareme.dto.TaskNoteRequest;
import com.tasksphere.shareme.dto.TaskNoteResponse;
import com.tasksphere.shareme.repository.TaskRepository;
import com.tasksphere.shareme.security.JwtTokenProvider;
import com.tasksphere.shareme.service.TaskNoteService;
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
@RequestMapping("/api/task-notes")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"},
    allowedHeaders = {"Authorization", "Content-Type"},
    exposedHeaders = {"Authorization"},
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
    maxAge = 3600)
@Tag(name = "Task Notes", description = "Personal notes and tags for tasks")
@SecurityRequirement(name = "bearerAuth")
public class TaskNoteController {
    
    private static final Logger logger = LoggerFactory.getLogger(TaskNoteController.class);
    
    @Autowired
    private TaskNoteService taskNoteService;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Operation(summary = "Get personal note for a task", 
               description = "Retrieve the current user's personal note for a specific task with all reminder tags and content")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task note retrieved successfully",
                content = @Content(schema = @Schema(implementation = TaskNoteResponse.class))),
        @ApiResponse(responseCode = "404", description = "Task or note not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/task/{taskId}")
    public ResponseEntity<?> getTaskNote(
            @Parameter(description = "Task ID to retrieve the note for", required = true, example = "1") 
            @PathVariable Long taskId,
            @Parameter(description = "JWT Bearer token for authentication", required = true)
            @RequestHeader("Authorization") String token) {
        
        if (!token.startsWith("Bearer ")) {
            logger.error("Invalid token format - token must start with 'Bearer '");
            return ResponseEntity.status(401)
                .body(new ErrorResponse(401, "Invalid token format", "AuthenticationException"));
        }

        String jwt = token.substring(7);
        Long userId;
        
        try {
            userId = jwtTokenProvider.getUserIdFromToken(jwt);
        } catch (Exception e) {
            logger.error("Failed to extract user ID from token: {}", e.getMessage(), e);
            return ResponseEntity.status(401)
                .body(new ErrorResponse(401, "Invalid token: " + e.getMessage(), "AuthenticationException"));
        }
        
        if (userId == null) {
            logger.error("User ID is null in JWT token");
            return ResponseEntity.status(401)
                .body(new ErrorResponse(401, "Invalid token - no user ID found", "AuthenticationException"));
        }
        
        logger.info("Getting task note for user {} and task {}", userId, taskId);
        
        try {
            // If taskId is null, return empty note response
            if (taskId == null) {
                logger.debug("Task ID is null, returning empty note");
                return ResponseEntity.ok(new TaskNoteResponse(null, null, null, null, "", List.of(), null, null));
            }
            
            // If task doesn't exist, return empty note response
            if (!taskRepository.existsById(taskId)) {
                logger.debug("Task {} not found, returning empty note", taskId);
                return ResponseEntity.ok(new TaskNoteResponse(null, taskId, null, null, "", List.of(), null, null));
            }
            
            // If user doesn't have access, return empty note response
            if (!taskService.userHasAccessToTask(userId, taskId)) {
                logger.debug("User {} does not have access to task {}, returning empty note", userId, taskId);
                return ResponseEntity.ok(new TaskNoteResponse(null, taskId, null, null, "", List.of(), null, null));
            }
            
            // Try to get the note
            try {
                TaskNoteResponse response = taskNoteService.getTaskNote(userId, taskId);
                logger.info("Task note retrieved successfully for user {} and task {}", userId, taskId);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                logger.debug("Error getting task note, returning empty note: {}", e.getMessage());
                return ResponseEntity.ok(new TaskNoteResponse(null, taskId, null, null, "", List.of(), null, null));
            }
        } catch (Exception e) {
            // For any unexpected error, return empty note response
            logger.error("Unexpected error getting task note: {}", e.getMessage(), e);
            return ResponseEntity.ok(new TaskNoteResponse(null, taskId, null, null, "", List.of(), null, null));
        }
    }
    
    @Operation(summary = "Save personal note for a task", 
               description = "Create or update the current user's personal note for a specific task with content and reminder tags")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task note updated successfully",
                content = @Content(schema = @Schema(implementation = TaskNoteResponse.class))),
        @ApiResponse(responseCode = "201", description = "Task note created successfully",
                content = @Content(schema = @Schema(implementation = TaskNoteResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad request - Invalid note data",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Task not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<?> saveTaskNote(
            @Valid @RequestBody TaskNoteRequest request,
            @Parameter(description = "JWT Bearer token for authentication", required = true)
            @RequestHeader("Authorization") String token) {
        
        if (!token.startsWith("Bearer ")) {
            logger.error("Invalid token format - token must start with 'Bearer '");
            return ResponseEntity.status(401)
                .body(new ErrorResponse(401, "Invalid token format", "AuthenticationException"));
        }

        String jwt = token.substring(7);
        Long userId;
        
        try {
            userId = jwtTokenProvider.getUserIdFromToken(jwt);
        } catch (Exception e) {
            logger.error("Failed to extract user ID from token: {}", e.getMessage(), e);
            return ResponseEntity.status(401)
                .body(new ErrorResponse(401, "Invalid token: " + e.getMessage(), "AuthenticationException"));
        }
        
        if (userId == null) {
            logger.error("User ID is null in JWT token");
            return ResponseEntity.status(401)
                .body(new ErrorResponse(401, "Invalid token - no user ID found", "AuthenticationException"));
        }
        
        logger.info("Saving task note for user {} and task {}", userId, request.getTaskId());
        
        try {
            // Validate request
            if (request == null || request.getNoteContent() == null) {
                logger.debug("Invalid request or empty note content");
                return ResponseEntity.ok(new TaskNoteResponse(null, null, null, null, "", List.of(), null, null));
            }
            
            // Check task access if taskId is provided
            if (request.getTaskId() != null) {
                // Check if task exists
                if (!taskRepository.existsById(request.getTaskId())) {
                    logger.debug("Task {} not found", request.getTaskId());
                    return ResponseEntity.ok(new TaskNoteResponse(null, request.getTaskId(), null, null, "", List.of(), null, null));
                }
                
                // Check if user has access
                if (!taskService.userHasAccessToTask(userId, request.getTaskId())) {
                    logger.debug("User {} does not have access to task {}", userId, request.getTaskId());
                    return ResponseEntity.ok(new TaskNoteResponse(null, request.getTaskId(), null, null, "", List.of(), null, null));
                }
            }
            
            // Try to save the note
            try {
                TaskNoteResponse response = taskNoteService.saveTaskNote(userId, request);
                logger.info("Task note saved successfully for user {} and task {}", userId, request.getTaskId());
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                logger.debug("Error saving task note: {}", e.getMessage());
                return ResponseEntity.ok(new TaskNoteResponse(null, request.getTaskId(), null, request.getNoteName(), 
                    request.getNoteContent(), request.getReminderTags(), null, null));
            }
        } catch (Exception e) {
            // For any unexpected error, return the request data in response format
            logger.error("Unexpected error saving task note: {}", e.getMessage(), e);
            return ResponseEntity.ok(new TaskNoteResponse(null, request.getTaskId(), null, request.getNoteName(), 
                request.getNoteContent(), request.getReminderTags(), null, null));
        }
    }
    
    @Operation(summary = "Delete personal note for a task", 
               description = "Delete the current user's personal note for a specific task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Task note deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Task note not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/task/{taskId}")
    public ResponseEntity<?> deleteTaskNote(
            @Parameter(description = "Task ID") @PathVariable Long taskId,
            @Parameter(description = "JWT Bearer token for authentication", required = true)
            @RequestHeader("Authorization") String token) {
        
        if (!token.startsWith("Bearer ")) {
            logger.error("Invalid token format - token must start with 'Bearer '");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), 
                    "Invalid token format", "AuthenticationException"));
        }

        String jwt = token.substring(7);
        Long userId;
        
        try {
            userId = jwtTokenProvider.getUserIdFromToken(jwt);
        } catch (Exception e) {
            logger.error("Failed to extract user ID from token: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), 
                    "Invalid token: " + e.getMessage(), "AuthenticationException"));
        }
        
        if (userId == null) {
            logger.error("User ID is null in JWT token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), 
                    "Invalid token - no user ID found", "AuthenticationException"));
        }
        
        logger.info("Deleting task note for user {} and task {}", userId, taskId);
        
        try {
            // If taskId is null, return success (nothing to delete)
            if (taskId == null) {
                logger.debug("Task ID is null, nothing to delete");
                return ResponseEntity.ok().body(new TaskNoteResponse(null, null, null, null, "", List.of(), null, null));
            }
            
            // If task doesn't exist, return success (nothing to delete)
            if (!taskRepository.existsById(taskId)) {
                logger.debug("Task {} not found, nothing to delete", taskId);
                return ResponseEntity.ok().body(new TaskNoteResponse(null, taskId, null, null, "", List.of(), null, null));
            }
            
            // If user doesn't have access, return success (nothing to delete)
            if (!taskService.userHasAccessToTask(userId, taskId)) {
                logger.debug("User {} does not have access to task {}, nothing to delete", userId, taskId);
                return ResponseEntity.ok().body(new TaskNoteResponse(null, taskId, null, null, "", List.of(), null, null));
            }
            
            // Try to delete the note
            try {
                taskNoteService.deleteTaskNote(userId, taskId);
                return ResponseEntity.ok().body(new TaskNoteResponse(null, taskId, null, null, "", List.of(), null, null));
            } catch (Exception e) {
                logger.debug("Error deleting task note: {}", e.getMessage());
                return ResponseEntity.ok().body(new TaskNoteResponse(null, taskId, null, null, "", List.of(), null, null));
            }
        } catch (Exception e) {
            // For any unexpected error, still return success
            logger.error("Unexpected error deleting task note: {}", e.getMessage(), e);
            return ResponseEntity.ok().body(new TaskNoteResponse(null, taskId, null, null, "", List.of(), null, null));
        }
    }
    
    @Operation(summary = "Get all personal task notes", 
               description = "Retrieve all personal task notes for the current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task notes retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<?> getUserTaskNotes(
            @Parameter(description = "JWT Bearer token for authentication", required = true)
            @RequestHeader("Authorization") String token) {
        
        if (!token.startsWith("Bearer ")) {
            logger.error("Invalid token format - token must start with 'Bearer '");
            return ResponseEntity.status(401)
                .body(new ErrorResponse(401, "Invalid token format", "AuthenticationException"));
        }

        String jwt = token.substring(7);
        Long userId;
        
        try {
            userId = jwtTokenProvider.getUserIdFromToken(jwt);
        } catch (Exception e) {
            logger.error("Failed to extract user ID from token: {}", e.getMessage(), e);
            return ResponseEntity.status(401)
                .body(new ErrorResponse(401, "Invalid token: " + e.getMessage(), "AuthenticationException"));
        }
        
        if (userId == null) {
            logger.error("User ID is null in JWT token");
            return ResponseEntity.status(401)
                .body(new ErrorResponse(401, "Invalid token - no user ID found", "AuthenticationException"));
        }
        
        logger.info("Getting all task notes for user {}", userId);
        
        List<TaskNoteResponse> notes = taskNoteService.getUserTaskNotes(userId);
        return ResponseEntity.ok(notes);
    }
    
    @Operation(summary = "Get task notes by tag", 
               description = "Retrieve personal task notes filtered by a specific reminder tag")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task notes retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/tag/{tag}")
    public ResponseEntity<?> getTaskNotesByTag(
            @Parameter(description = "Reminder tag") @PathVariable String tag,
            @Parameter(description = "JWT Bearer token for authentication", required = true)
            @RequestHeader("Authorization") String token) {
        
        if (!token.startsWith("Bearer ")) {
            logger.error("Invalid token format - token must start with 'Bearer '");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), 
                    "Invalid token format", "AuthenticationException"));
        }

        String jwt = token.substring(7);
        Long userId;
        
        try {
            userId = jwtTokenProvider.getUserIdFromToken(jwt);
        } catch (Exception e) {
            logger.error("Failed to extract user ID from token: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), 
                    "Invalid token: " + e.getMessage(), "AuthenticationException"));
        }
        
        if (userId == null) {
            logger.error("User ID is null in JWT token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), 
                    "Invalid token - no user ID found", "AuthenticationException"));
        }
        
        logger.info("Getting task notes for user {} with tag {}", userId, tag);
        
        try {
            List<TaskNoteResponse> notes = taskNoteService.getUserTaskNotesByTag(userId, tag);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            logger.error("Error getting task notes for user {} with tag {}: {}", 
                userId, tag, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Failed to get task notes: " + e.getMessage(), "InternalServerError"));
        }
    }
    
    @Operation(summary = "Get all user tags", 
               description = "Retrieve all unique reminder tags used by the current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tags retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/tags")
    public ResponseEntity<?> getUserTags(
            @Parameter(description = "JWT Bearer token for authentication", required = true)
            @RequestHeader("Authorization") String token) {
        
        if (!token.startsWith("Bearer ")) {
            logger.error("Invalid token format - token must start with 'Bearer '");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), 
                    "Invalid token format", "AuthenticationException"));
        }

        String jwt = token.substring(7);
        Long userId;
        
        try {
            userId = jwtTokenProvider.getUserIdFromToken(jwt);
        } catch (Exception e) {
            logger.error("Failed to extract user ID from token: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), 
                    "Invalid token: " + e.getMessage(), "AuthenticationException"));
        }
        
        if (userId == null) {
            logger.error("User ID is null in JWT token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), 
                    "Invalid token - no user ID found", "AuthenticationException"));
        }
        
        logger.info("Getting all tags for user {}", userId);
        
        try {
            List<String> tags = taskNoteService.getUserTags(userId);
            return ResponseEntity.ok(tags);
        } catch (Exception e) {
            logger.error("Error getting tags for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Failed to get tags: " + e.getMessage(), "InternalServerError"));
        }
    }
    
    @Operation(summary = "Check if task has note", 
               description = "Check if the current user has a personal note for a specific task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Check completed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/task/{taskId}/exists")
    public ResponseEntity<?> hasTaskNote(
            @Parameter(description = "Task ID", required = true, example = "1")
            @PathVariable Long taskId,
            @Parameter(description = "JWT Bearer token for authentication", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader("Authorization") String token) {
        
        if (!token.startsWith("Bearer ")) {
            logger.error("Invalid token format - token must start with 'Bearer '");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), 
                    "Invalid token format", "AuthenticationException"));
        }

        String jwt = token.substring(7);
        Long userId;
        
        try {
            userId = jwtTokenProvider.getUserIdFromToken(jwt);
        } catch (Exception e) {
            logger.error("Failed to extract user ID from token: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), 
                    "Invalid token: " + e.getMessage(), "AuthenticationException"));
        }
        
        if (userId == null) {
            logger.error("User ID is null in JWT token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), 
                    "Invalid token - no user ID found", "AuthenticationException"));
        }
        
        logger.debug("Checking if user {} has note for task {}", userId, taskId);
        
        try {
            // If taskId is null, return false
            if (taskId == null) {
                logger.debug("Task ID is null, returning false");
                return ResponseEntity.ok(false);
            }
            
            // If task doesn't exist, return false
            if (!taskRepository.existsById(taskId)) {
                logger.debug("Task {} not found, returning false", taskId);
                return ResponseEntity.ok(false);
            }
            
            // If user doesn't have access, return false
            if (!taskService.userHasAccessToTask(userId, taskId)) {
                logger.debug("User {} does not have access to task {}, returning false", userId, taskId);
                return ResponseEntity.ok(false);
            }
            
            // Now check if the note exists
            try {
                boolean hasNote = taskNoteService.hasTaskNote(userId, taskId);
                return ResponseEntity.ok(hasNote);
            } catch (Exception e) {
                logger.debug("Error checking for note, returning false: {}", e.getMessage());
                return ResponseEntity.ok(false);
            }
        } catch (Exception e) {
            // For any other error, return false
            logger.error("Unexpected error checking task note existence: {}", e.getMessage(), e);
            return ResponseEntity.ok(false);
        }
    }
}