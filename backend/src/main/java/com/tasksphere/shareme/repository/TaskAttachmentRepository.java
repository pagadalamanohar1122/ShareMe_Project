package com.tasksphere.shareme.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tasksphere.shareme.entity.TaskAttachment;

@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {
    
    List<TaskAttachment> findByTaskIdOrderByUploadedAtDesc(Long taskId);
    
    @Query("SELECT COUNT(ta) FROM TaskAttachment ta WHERE ta.task.id = :taskId")
    Long countByTaskId(@Param("taskId") Long taskId);
    
    @Query("SELECT SUM(ta.fileSize) FROM TaskAttachment ta WHERE ta.task.id = :taskId")
    Long getTotalFileSizeByTaskId(@Param("taskId") Long taskId);
    
    List<TaskAttachment> findByUploadedById(Long userId);
    
    @Query("SELECT ta FROM TaskAttachment ta WHERE ta.task.id = :taskId AND ta.uploadedBy.id = :userId")
    List<TaskAttachment> findByTaskIdAndUploadedById(@Param("taskId") Long taskId, @Param("userId") Long userId);
}