package com.tasksphere.shareme.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tasksphere.shareme.entity.Project;
import com.tasksphere.shareme.entity.User;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    List<Project> findByOwnerOrderByCreatedAtDesc(User owner);
    
    @Query("SELECT p FROM Project p JOIN p.members m WHERE m.id = :userId ORDER BY p.createdAt DESC")
    List<Project> findProjectsByMemberId(@Param("userId") Long userId);
    
    @Query("SELECT p FROM Project p WHERE p.owner.id = :userId OR :userId IN (SELECT m.id FROM p.members m) ORDER BY p.createdAt DESC")
    List<Project> findAllUserProjects(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(p) FROM Project p WHERE p.owner.id = :userId OR :userId IN (SELECT m.id FROM p.members m)")
    Long countUserProjects(@Param("userId") Long userId);
    
    List<Project> findByStatusOrderByCreatedAtDesc(Project.ProjectStatus status);
}