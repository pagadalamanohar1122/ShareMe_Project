package com.tasksphere.shareme.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tasksphere.shareme.dto.TaskAttachmentResponse;
import com.tasksphere.shareme.entity.User;
import com.tasksphere.shareme.service.TaskAttachmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Attachments", description = "Upload, list, download, delete task files")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "http://localhost:3000")
public class TaskAttachmentController {

    @Autowired
    private TaskAttachmentService taskAttachmentService;

    @PostMapping("/{taskId}/attachments")
    @Operation(summary = "Upload files to a task", description = "Upload one or more files as attachments to a task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Files uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file or request"),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "413", description = "File too large")
    })
    public ResponseEntity<List<TaskAttachmentResponse>> uploadFiles(
            @Parameter(description = "Task ID") @PathVariable Long taskId,
            @Parameter(description = "Files to upload") @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal User user) {
        try {
            List<TaskAttachmentResponse> responses = taskAttachmentService.uploadFiles(taskId, files, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(responses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{taskId}/attachments/single")
    @Operation(summary = "Upload single file to a task", description = "Upload a single file as attachment to a task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "File uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file or request"),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "413", description = "File too large")
    })
    public ResponseEntity<TaskAttachmentResponse> uploadSingleFile(
            @Parameter(description = "Task ID") @PathVariable Long taskId,
            @Parameter(description = "File to upload") @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user) {
        try {
            // Create a task to get the task object, then upload the file
            // This is a simplified approach - in a real app you'd fetch the task first
            List<MultipartFile> files = List.of(file);
            List<TaskAttachmentResponse> responses = taskAttachmentService.uploadFiles(taskId, files, user);
            
            if (!responses.isEmpty()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(responses.get(0));
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{taskId}/attachments")
    @Operation(summary = "Get task attachments", description = "Retrieve all attachments for a specific task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Attachments retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<List<TaskAttachmentResponse>> getTaskAttachments(
            @Parameter(description = "Task ID") @PathVariable Long taskId) {
        try {
            List<TaskAttachmentResponse> attachments = taskAttachmentService.getTaskAttachments(taskId);
            return ResponseEntity.ok(attachments);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/attachments/{attachmentId}/download")
    @Operation(summary = "Download attachment", description = "Download a specific attachment file")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "File downloaded successfully"),
        @ApiResponse(responseCode = "404", description = "Attachment not found"),
        @ApiResponse(responseCode = "500", description = "Error downloading file")
    })
    public ResponseEntity<Resource> downloadAttachment(
            @Parameter(description = "Attachment ID") @PathVariable Long attachmentId) {
        try {
            Resource resource = taskAttachmentService.downloadFile(attachmentId);
            
            // Try to determine file's content type
            String contentType = "application/octet-stream";
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/attachments/{attachmentId}")
    @Operation(summary = "Delete attachment", description = "Delete a specific attachment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Attachment deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Not authorized to delete this attachment"),
        @ApiResponse(responseCode = "404", description = "Attachment not found")
    })
    public ResponseEntity<Void> deleteAttachment(
            @Parameter(description = "Attachment ID") @PathVariable Long attachmentId,
            @AuthenticationPrincipal User user) {
        try {
            taskAttachmentService.deleteAttachment(attachmentId, user);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("permission")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } else if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }

    @GetMapping("/{taskId}/attachments/stats")
    @Operation(summary = "Get attachment statistics", description = "Get attachment count and total file size for a task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<Map<String, Object>> getAttachmentStats(
            @Parameter(description = "Task ID") @PathVariable Long taskId) {
        try {
            long count = taskAttachmentService.getAttachmentCount(taskId);
            long totalSize = taskAttachmentService.getTotalFileSize(taskId);
            
            Map<String, Object> stats = Map.of(
                "attachmentCount", count,
                "totalFileSize", totalSize,
                "totalFileSizeFormatted", formatFileSize(totalSize)
            );
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Format file size for human readable display
     */
    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        else if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        else if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024));
        else return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
    }
}