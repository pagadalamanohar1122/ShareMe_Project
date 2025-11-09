package com.tasksphere.shareme.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tasksphere.shareme.dto.TaskAttachmentResponse;
import com.tasksphere.shareme.dto.UserInfo;
import com.tasksphere.shareme.entity.Task;
import com.tasksphere.shareme.entity.TaskAttachment;
import com.tasksphere.shareme.entity.User;
import com.tasksphere.shareme.exception.ResourceNotFoundException;
import com.tasksphere.shareme.repository.TaskAttachmentRepository;
import com.tasksphere.shareme.repository.TaskRepository;

@Service
public class TaskAttachmentService {

    @Autowired
    private TaskAttachmentRepository taskAttachmentRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final String[] ALLOWED_EXTENSIONS = {
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt",
        "jpg", "jpeg", "png", "gif", "bmp", "svg",
        "mp4", "avi", "mov", "wmv", "flv", "webm"
    };

    /**
     * Upload multiple files for a task
     */
    public List<TaskAttachmentResponse> uploadFiles(Long taskId, List<MultipartFile> files, User uploader) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        return files.stream()
            .map(file -> uploadSingleFile(task, file, uploader))
            .collect(Collectors.toList());
    }

    /**
     * Upload a single file
     */
    public TaskAttachmentResponse uploadSingleFile(Task task, MultipartFile file, User uploader) {
        validateFile(file);

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String storedFilename = UUID.randomUUID().toString() + "." + fileExtension;
            
            // Create file path
            Path filePath = uploadPath.resolve(storedFilename);
            
            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Create attachment entity
            TaskAttachment attachment = new TaskAttachment();
            attachment.setTask(task);
            attachment.setOriginalFilename(originalFilename);
            attachment.setStoredFilename(storedFilename);
            attachment.setFilePath(filePath.toString());
            attachment.setFileSize(file.getSize());
            attachment.setContentType(file.getContentType());
            attachment.setUploadedBy(uploader);
            attachment.setUploadedAt(LocalDateTime.now());

            // Save to database
            TaskAttachment savedAttachment = taskAttachmentRepository.save(attachment);

            return convertToResponse(savedAttachment);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + file.getOriginalFilename(), e);
        }
    }

    /**
     * Get all attachments for a task
     */
    public List<TaskAttachmentResponse> getTaskAttachments(Long taskId) {
        List<TaskAttachment> attachments = taskAttachmentRepository.findByTaskIdOrderByUploadedAtDesc(taskId);
        return attachments.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Download a file
     */
    public Resource downloadFile(Long attachmentId) {
        TaskAttachment attachment = taskAttachmentRepository.findById(attachmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with id: " + attachmentId));

        try {
            Path filePath = Paths.get(attachment.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found or not readable: " + attachment.getOriginalFilename());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error downloading file: " + attachment.getOriginalFilename(), e);
        }
    }

    /**
     * Delete an attachment
     */
    public void deleteAttachment(Long attachmentId, User user) {
        TaskAttachment attachment = taskAttachmentRepository.findById(attachmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with id: " + attachmentId));

        // Check if user has permission to delete (task creator, assignee, or file uploader)
        Task task = attachment.getTask();
        boolean canDelete = task.getCreator().getId().equals(user.getId()) ||
                           (task.getAssignee() != null && task.getAssignee().getId().equals(user.getId())) ||
                           attachment.getUploadedBy().getId().equals(user.getId());

        if (!canDelete) {
            throw new RuntimeException("You don't have permission to delete this attachment");
        }

        try {
            // Delete file from storage
            Path filePath = Paths.get(attachment.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }

            // Delete from database
            taskAttachmentRepository.delete(attachment);

        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + attachment.getOriginalFilename(), e);
        }
    }

    /**
     * Get attachment count for a task
     */
    public long getAttachmentCount(Long taskId) {
        return taskAttachmentRepository.countByTaskId(taskId);
    }

    /**
     * Get total file size for a task
     */
    public long getTotalFileSize(Long taskId) {
        return taskAttachmentRepository.getTotalFileSizeByTaskId(taskId);
    }

    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File size exceeds maximum limit of 50MB");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new RuntimeException("Invalid filename");
        }

        String extension = getFileExtension(filename).toLowerCase();
        boolean isAllowed = false;
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (allowedExt.equals(extension)) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed) {
            throw new RuntimeException("File type not allowed. Allowed types: pdf, doc, docx, xls, xlsx, ppt, pptx, txt, jpg, jpeg, png, gif, bmp, svg, mp4, avi, mov, wmv, flv, webm");
        }
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    /**
     * Convert TaskAttachment entity to response DTO
     */
    private TaskAttachmentResponse convertToResponse(TaskAttachment attachment) {
        UserInfo uploaderInfo = new UserInfo(
            attachment.getUploadedBy().getId(),
            attachment.getUploadedBy().getFirstName(),
            attachment.getUploadedBy().getLastName(),
            attachment.getUploadedBy().getEmail(),
            attachment.getUploadedBy().getRole().toString()
        );

        String downloadUrl = "/api/tasks/attachments/" + attachment.getId() + "/download";

        return new TaskAttachmentResponse(
            attachment.getId(),
            attachment.getOriginalFilename(),
            attachment.getFileSize(),
            attachment.getContentType(),
            uploaderInfo,
            attachment.getUploadedAt(),
            downloadUrl
        );
    }
}