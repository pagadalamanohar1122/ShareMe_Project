package com.tasksphere.shareme.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tasksphere.shareme.dto.CreateProjectBasicRequest;
import com.tasksphere.shareme.dto.CreateProjectRequest;
import com.tasksphere.shareme.dto.ProjectResponse;
import com.tasksphere.shareme.dto.UserInfo;
import com.tasksphere.shareme.entity.Priority;
import com.tasksphere.shareme.entity.Project;
import com.tasksphere.shareme.entity.ProjectDocument;
import com.tasksphere.shareme.entity.Task;
import com.tasksphere.shareme.entity.User;
import com.tasksphere.shareme.exception.ProjectException;
import com.tasksphere.shareme.repository.ProjectRepository;
import com.tasksphere.shareme.repository.TaskRepository;
import com.tasksphere.shareme.repository.UserRepository;

@Service
@Transactional
public class ProjectService {
    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentStorageService documentStorageService;

    public List<ProjectResponse> getUserProjects(Long userId) {
        List<Project> projects = projectRepository.findAllUserProjects(userId);
        return projects.stream()
                .map(this::convertToProjectResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse createProject(CreateProjectBasicRequest request, Long ownerId) {
        logger.debug("Creating project with basic details - name: {}, priority: {}, description length: {}", 
            request.getName(), 
            request.getPriority(), 
            request.getDescription() != null ? request.getDescription().length() : 0);
            
        Optional<User> ownerOpt = userRepository.findById(ownerId);
        if (ownerOpt.isEmpty()) {
            logger.error("User not found with ID: {}", ownerId);
            throw new ProjectException("User not found", HttpStatus.NOT_FOUND);
        }

        User owner = ownerOpt.get();
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwner(owner);
        project.setCreatedAt(LocalDateTime.now());
        project.setStatus(Project.ProjectStatus.ACTIVE);
        
        // Handle deadline conversion
        if (request.getDeadline() != null && !request.getDeadline().trim().isEmpty()) {
            try {
                LocalDateTime deadline = LocalDateTime.parse(request.getDeadline());
                project.setDeadline(deadline);
            } catch (Exception e) {
                logger.error("Invalid deadline format", e);
                throw new ProjectException("Invalid deadline format. Expected ISO-8601 format", HttpStatus.BAD_REQUEST);
            }
        }
        
        try {
            project.setPriority(Priority.valueOf(request.getPriority().toUpperCase()));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid priority value: {}", request.getPriority(), e);
            throw new ProjectException("Invalid priority value: " + request.getPriority(), HttpStatus.BAD_REQUEST);
        }

        // Add project members
        if (request.getMemberEmails() != null && !request.getMemberEmails().isEmpty()) {
            List<User> members = userRepository.findAllByEmailIn(request.getMemberEmails());
            if (members.isEmpty()) {
                logger.error("No valid users found for emails: {}", request.getMemberEmails());
                throw new ProjectException("No valid users found for the provided email addresses", HttpStatus.BAD_REQUEST);
            }
            project.setMembers(members);
        }

        try {
            Project savedProject = projectRepository.save(project);
            logger.info("Successfully created project with ID: {}", savedProject.getId());
            return convertToProjectResponse(savedProject);
        } catch (Exception e) {
            logger.error("Failed to create project with name: {}", request.getName(), e);
            throw new ProjectException("Failed to create project: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ProjectResponse addDocumentsToProject(Long projectId, List<MultipartFile> documents, Long userId) {
        logger.debug("Adding {} documents to project {}", documents.size(), projectId);

        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            logger.error("Project not found with ID: {}", projectId);
            throw new ProjectException("Project not found", HttpStatus.NOT_FOUND);
        }

        Project project = projectOpt.get();
        
        // Only project owner or members can add documents
        boolean hasAccess = project.getOwner().getId().equals(userId) ||
                project.getMembers().stream().anyMatch(member -> member.getId().equals(userId));
        
        if (!hasAccess) {
            logger.error("User {} does not have access to project {}", userId, projectId);
            throw new ProjectException("Access denied to project", HttpStatus.FORBIDDEN);
        }

        try {
            for (MultipartFile document : documents) {
                String fileName = document.getOriginalFilename();
                String filePath = documentStorageService.storeFile(document);
                ProjectDocument doc = new ProjectDocument(fileName, filePath, project, userId);
                project.getDocuments().add(doc);
                logger.debug("Added document {} to project {}", fileName, projectId);
            }

            Project savedProject = projectRepository.save(project);
            logger.info("Successfully added {} documents to project {}", documents.size(), projectId);
            return convertToProjectResponse(savedProject);
        } catch (Exception e) {
            logger.error("Failed to add documents to project {}", projectId, e);
            throw new ProjectException("Failed to add documents: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ProjectResponse getProjectById(Long projectId, Long userId) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            throw new ProjectException("Project not found", HttpStatus.NOT_FOUND);
        }

        Project project = projectOpt.get();
        
        // Check if user has access to this project
        boolean hasAccess = project.getOwner().getId().equals(userId) ||
                project.getMembers().stream().anyMatch(member -> member.getId().equals(userId));
        
        if (!hasAccess) {
            throw new ProjectException("Access denied to project", HttpStatus.FORBIDDEN);
        }

        return convertToProjectResponse(project);
    }

    public ProjectResponse updateProject(Long projectId, CreateProjectRequest request, Long userId) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            throw new ProjectException("Project not found", HttpStatus.NOT_FOUND);
        }

        Project project = projectOpt.get();
        
        // Only owner can update project
        if (!project.getOwner().getId().equals(userId)) {
            throw new ProjectException("Only project owner can update the project", HttpStatus.FORBIDDEN);
        }

        try {
            project.setName(request.getName());
            project.setDescription(request.getDescription());
            
            // Handle deadline conversion
            if (request.getDeadline() != null && !request.getDeadline().trim().isEmpty()) {
                try {
                    LocalDateTime deadline = LocalDateTime.parse(request.getDeadline());
                    project.setDeadline(deadline);
                } catch (Exception e) {
                    throw new ProjectException("Invalid deadline format. Expected ISO-8601 format", HttpStatus.BAD_REQUEST);
                }
            } else {
                project.setDeadline(null);
            }

            Project savedProject = projectRepository.save(project);
            return convertToProjectResponse(savedProject);
        } catch (Exception e) {
            throw new ProjectException("Failed to update project: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ProjectResponse addDocumentToProject(Long projectId, MultipartFile document, Long userId) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            logger.error("Project not found with ID: {}", projectId);
            throw new ProjectException("Project not found", HttpStatus.NOT_FOUND);
        }

        Project project = projectOpt.get();
        
        // Check if user has access to this project
        boolean hasAccess = project.getOwner().getId().equals(userId) ||
                project.getMembers().stream().anyMatch(member -> member.getId().equals(userId));
        
        if (!hasAccess) {
            logger.error("User {} does not have access to project {}", userId, projectId);
            throw new ProjectException("Access denied to project", HttpStatus.FORBIDDEN);
        }

        try {
            String fileName = document.getOriginalFilename();
            String filePath = documentStorageService.storeFile(document);
            ProjectDocument doc = new ProjectDocument(fileName, filePath, project, userId);
            project.getDocuments().add(doc);
            Project savedProject = projectRepository.save(project);
            logger.info("Successfully added document {} to project {}", fileName, projectId);
            return convertToProjectResponse(savedProject);
        } catch (Exception e) {
            logger.error("Failed to add document to project {}", projectId, e);
            throw new ProjectException("Failed to add document: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void deleteProject(Long projectId, Long userId) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            throw new ProjectException("Project not found", HttpStatus.NOT_FOUND);
        }

        Project project = projectOpt.get();
        
        // Only owner can delete project
        if (!project.getOwner().getId().equals(userId)) {
            throw new ProjectException("Only project owner can delete the project", HttpStatus.FORBIDDEN);
        }

        try {
            projectRepository.delete(project);
        } catch (Exception e) {
            throw new ProjectException("Failed to delete project: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Long getProjectCount(Long userId) {
        return projectRepository.countUserProjects(userId);
    }

    public Long getCompletedTasksCount(Long userId) {
        return taskRepository.countUserTasksByStatus(userId, Task.TaskStatus.COMPLETED);
    }

    public Long getInProgressTasksCount(Long userId) {
        return taskRepository.countUserTasksByStatus(userId, Task.TaskStatus.IN_PROGRESS);
    }

    private ProjectResponse convertToProjectResponse(Project project) {
        UserInfo ownerInfo = new UserInfo(
                project.getOwner().getId(),
                project.getOwner().getFirstName(),
                project.getOwner().getLastName(),
                project.getOwner().getEmail(),
                project.getOwner().getRole().toString()
        );

        ProjectResponse response = new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                ownerInfo,
                project.getCreatedAt(),
                project.getDeadline()
        );

        response.setUpdatedAt(project.getUpdatedAt());

        // Set task counts
        Long totalTasks = taskRepository.countTasksByProjectId(project.getId());
        Long completedTasks = taskRepository.countTasksByProjectIdAndStatus(project.getId(), Task.TaskStatus.COMPLETED);
        Long inProgressTasks = taskRepository.countTasksByProjectIdAndStatus(project.getId(), Task.TaskStatus.IN_PROGRESS);

        response.setTotalTasks(totalTasks.intValue());
        response.setCompletedTasks(completedTasks.intValue());
        response.setInProgressTasks(inProgressTasks.intValue());

        // Set members if needed
        if (project.getMembers() != null) {
            List<UserInfo> members = project.getMembers().stream()
                    .map(member -> new UserInfo(
                            member.getId(),
                            member.getFirstName(),
                            member.getLastName(),
                            member.getEmail(),
                            member.getRole().toString()
                    ))
                    .collect(Collectors.toList());
            response.setMembers(members);
        }

        return response;
    }
}