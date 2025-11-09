package com.tasksphere.shareme.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tasksphere.shareme.entity.Project;
import com.tasksphere.shareme.entity.Task;
import com.tasksphere.shareme.entity.User;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    List<Task> findByProjectOrderByCreatedAtDesc(Project project);
    
    List<Task> findByAssigneeOrderByCreatedAtDesc(User assignee);
    
    List<Task> findByCreatorOrderByCreatedAtDesc(User creator);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId")
    Long countTasksByProjectId(@Param("projectId") Long projectId);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.status = :status")
    Long countTasksByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") Task.TaskStatus status);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignee.id = :userId OR t.creator.id = :userId")
    Long countUserTasks(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE (t.assignee.id = :userId OR t.creator.id = :userId) AND t.status = :status")
    Long countUserTasksByStatus(@Param("userId") Long userId, @Param("status") Task.TaskStatus status);
    
    // Advanced search and filtering methods
    @Query("SELECT t FROM Task t WHERE " +
           "(:query IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:projectId IS NULL OR t.project.id = :projectId) AND " +
           "(:assigneeId IS NULL OR t.assignee.id = :assigneeId) AND " +
           "(:creatorId IS NULL OR t.creator.id = :creatorId)")
    Page<Task> findTasksWithFilters(@Param("query") String query,
                                   @Param("status") Task.TaskStatus status,
                                   @Param("priority") Task.TaskPriority priority,
                                   @Param("projectId") Long projectId,
                                   @Param("assigneeId") Long assigneeId,
                                   @Param("creatorId") Long creatorId,
                                   Pageable pageable);
    
    // Find tasks accessible by user (either assigned to them or created by them)
    @Query("SELECT t FROM Task t WHERE " +
           "(t.assignee.id = :userId OR t.creator.id = :userId OR t.project.owner.id = :userId) AND " +
           "(:query IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:projectId IS NULL OR t.project.id = :projectId)")
    Page<Task> findUserAccessibleTasksWithFilters(@Param("userId") Long userId,
                                                 @Param("query") String query,
                                                 @Param("status") Task.TaskStatus status,
                                                 @Param("priority") Task.TaskPriority priority,
                                                 @Param("projectId") Long projectId,
                                                 Pageable pageable);
    
    // Find tasks by status
    List<Task> findByStatusOrderByCreatedAtDesc(Task.TaskStatus status);
    
    // Find tasks by priority
    List<Task> findByPriorityOrderByCreatedAtDesc(Task.TaskPriority priority);
    
    // Find overdue tasks
    @Query("SELECT t FROM Task t WHERE t.dueDate < CURRENT_TIMESTAMP AND t.status != 'COMPLETED' AND t.status != 'CANCELLED'")
    List<Task> findOverdueTasks();
    
    // Find tasks due today or in the next few days  
    @Query("SELECT t FROM Task t WHERE t.dueDate >= CURRENT_DATE AND t.status != 'COMPLETED' AND t.status != 'CANCELLED'")
    List<Task> findTasksDueInDays(@Param("days") int days);
    
    // Find recent tasks for user
    @Query("SELECT t FROM Task t WHERE (t.assignee.id = :userId OR t.creator.id = :userId) ORDER BY t.createdAt DESC")
    Page<Task> findRecentTasksByUser(@Param("userId") Long userId, Pageable pageable);
}