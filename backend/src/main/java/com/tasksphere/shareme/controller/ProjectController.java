package com.tasksphere.shareme.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tasksphere.shareme.dto.CreateProjectBasicRequest;
import com.tasksphere.shareme.dto.CreateProjectRequest;
import com.tasksphere.shareme.dto.ErrorResponse;
import com.tasksphere.shareme.dto.ProjectResponse;
import com.tasksphere.shareme.exception.ProjectException;
import com.tasksphere.shareme.security.JwtTokenProvider;
import com.tasksphere.shareme.service.ProjectService;

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
@RequestMapping("/api/projects")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"})
@Tag(name = "Projects", description = "Create, read, update, delete projects and project stats")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    private ProjectService projectService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping
    @Operation(summary = "Get User Projects", description = "Retrieve all projects belonging to the authenticated user with complete project details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Projects retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<ProjectResponse>> getUserProjects(
            @Parameter(description = "JWT Bearer token for authentication", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7); // Remove "Bearer " prefix
            Long userId = jwtTokenProvider.getUserIdFromToken(jwt);

            List<ProjectResponse> projects = projectService.getUserProjects(userId);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createProject(
            @Valid @RequestBody CreateProjectBasicRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            LOGGER.info("Received request to create project: {}", request.getName());
            
            if (!token.startsWith("Bearer ")) {
                LOGGER.error("Invalid token format - token must start with 'Bearer '");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), 
                        "Invalid token format", "AuthenticationException"));
            }

            String jwt = token.substring(7);
            Long userId;
            
            try {
                userId = jwtTokenProvider.getUserIdFromToken(jwt);
            } catch (Exception e) {
                LOGGER.error("Failed to extract user ID from token: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), 
                        "Invalid token: " + e.getMessage(), "AuthenticationException"));
            }

            // Validate request
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                LOGGER.error("Project name is required");
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), 
                        "Project name is required", "ValidationException"));
            }

            // Log the received data
            LOGGER.info("Creating project - Name: {}, Priority: {}, Description: {}, Member Emails: {}", 
                request.getName(),
                request.getPriority(),
                request.getDescription(),
                request.getMemberEmails());

            ProjectResponse project = projectService.createProject(request, userId);
            LOGGER.info("Successfully created project with ID: {}", project.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(project);
            
        } catch (ProjectException e) {
            LOGGER.error("Project-specific error while creating project", e);
            ErrorResponse error = new ErrorResponse(
                e.getStatus().value(),
                e.getMessage(),
                "ProjectException"
            );
            // Use numeric status to avoid HttpStatusCode null-safety warning
            return ResponseEntity.status(e.getStatus().value()).body(error);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid input data while creating project", e);
            ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid input data: " + e.getMessage(),
                "ValidationException"
            );
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            LOGGER.error("Unexpected error while creating project: {}", e.getMessage(), e);
            ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred: " + e.getMessage(),
                e.getClass().getSimpleName()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/{projectId}/documents")
    @Operation(summary = "Upload Document", description = "Upload a document to an existing project")
    public ResponseEntity<?> uploadDocument(
            @PathVariable Long projectId,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7);
            Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
            ProjectResponse project = projectService.addDocumentToProject(projectId, file, userId);
            return ResponseEntity.ok(project);
        } catch (ProjectException e) {
            HttpStatus status = e.getStatus() != null ? e.getStatus() : HttpStatus.INTERNAL_SERVER_ERROR;
            ErrorResponse error = new ErrorResponse(
                status.value(),
                e.getMessage(),
                "ProjectException"
            );
            return ResponseEntity.status(status).body(error);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Failed to upload document: " + e.getMessage(),
                e.getClass().getSimpleName()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Project by ID", description = "Retrieve a specific project by its ID with full details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Project retrieved successfully",
                content = @Content(schema = @Schema(implementation = ProjectResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden - Access denied to this project",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Project not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getProject(
            @Parameter(description = "Project ID to retrieve", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "JWT Bearer token for authentication", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7); // Remove "Bearer " prefix
            Long userId = jwtTokenProvider.getUserIdFromToken(jwt);

            ProjectResponse project = projectService.getProjectById(id, userId);
            return ResponseEntity.ok(project);
        } catch (ProjectException e) {
            ErrorResponse error = new ErrorResponse(
                e.getStatus().value(),
                e.getMessage(),
                "ProjectException"
            );
            return ResponseEntity.status(e.getStatus().value()).body(error);
        } catch (Exception e) {
            LOGGER.error("Failed to get project", e);
            ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Failed to get project: " + e.getMessage(),
                e.getClass().getSimpleName()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Project", description = "Update an existing project (only project owner can update)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Project updated successfully",
                content = @Content(schema = @Schema(implementation = ProjectResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad request - Invalid project data",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only project owner can update",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Project not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> updateProject(
            @Parameter(description = "Project ID to update", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Updated project details", required = true)
            @Valid @RequestBody CreateProjectRequest request,
            @Parameter(description = "JWT Bearer token for authentication", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7); // Remove "Bearer " prefix
            Long userId = jwtTokenProvider.getUserIdFromToken(jwt);

            ProjectResponse project = projectService.updateProject(id, request, userId);
            return ResponseEntity.ok(project);
        } catch (ProjectException e) {
            ErrorResponse error = new ErrorResponse(
                e.getStatus().value(),
                e.getMessage(),
                "ProjectException"
            );
            return ResponseEntity.status(e.getStatus().value()).body(error);
        } catch (Exception e) {
            LOGGER.error("Failed to update project", e);
            ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Failed to update project: " + e.getMessage(),
                e.getClass().getSimpleName()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Project", description = "Delete a project permanently (only project owner can delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only project owner can delete",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Project not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> deleteProject(
            @Parameter(description = "Project ID to delete", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "JWT Bearer token for authentication", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7); // Remove "Bearer " prefix
            Long userId = jwtTokenProvider.getUserIdFromToken(jwt);

            projectService.deleteProject(id, userId);
            return ResponseEntity.noContent().build();
        } catch (ProjectException e) {
            ErrorResponse error = new ErrorResponse(
                e.getStatus().value(),
                e.getMessage(),
                "ProjectException"
            );
            return ResponseEntity.status(e.getStatus().value()).body(error);
        } catch (Exception e) {
            LOGGER.error("Failed to delete project", e);
            ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Failed to delete project: " + e.getMessage(),
                e.getClass().getSimpleName()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/stats")
    @Operation(summary = "Get User Statistics", description = "Retrieve comprehensive statistics about user's projects and tasks")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Map<String, Object>> getUserStats(
            @Parameter(description = "JWT Bearer token for authentication", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7); // Remove "Bearer " prefix
            Long userId = jwtTokenProvider.getUserIdFromToken(jwt);

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalProjects", projectService.getProjectCount(userId));
            stats.put("completedTasks", projectService.getCompletedTasksCount(userId));
            stats.put("inProgressTasks", projectService.getInProgressTasksCount(userId));

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}