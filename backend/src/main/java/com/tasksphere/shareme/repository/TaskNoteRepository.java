package com.tasksphere.shareme.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tasksphere.shareme.entity.TaskNote;

@Repository
public interface TaskNoteRepository extends JpaRepository<TaskNote, Long> {
    
    /**
     * Find a task note by user ID and task ID
     */
    Optional<TaskNote> findByUserIdAndTaskId(Long userId, Long taskId);
    
    /**
     * Find all task notes for a specific user
     */
    List<TaskNote> findByUserIdOrderByUpdatedAtDesc(Long userId);
    
    /**
     * Find all task notes for a specific task (admin/project manager use)
     */
    List<TaskNote> findByTaskIdOrderByUpdatedAtDesc(Long taskId);
    
    /**
     * Find task notes by user and containing specific tags
     */
    @Query("SELECT tn FROM TaskNote tn WHERE tn.user.id = :userId AND :tag MEMBER OF tn.reminderTags ORDER BY tn.updatedAt DESC")
    List<TaskNote> findByUserIdAndReminderTagsContaining(@Param("userId") Long userId, @Param("tag") String tag);
    
    /**
     * Find all unique tags used by a user
     */
    @Query("SELECT DISTINCT tag FROM TaskNote tn JOIN tn.reminderTags tag WHERE tn.user.id = :userId ORDER BY tag")
    List<String> findDistinctTagsByUserId(@Param("userId") Long userId);
    
    /**
     * Check if a user has a note for a specific task
     */
    boolean existsByUserIdAndTaskId(Long userId, Long taskId);
    
    /**
     * Delete task note by user ID and task ID
     */
    void deleteByUserIdAndTaskId(Long userId, Long taskId);
    
    /**
     * Count task notes for a user
     */
    long countByUserId(Long userId);
}