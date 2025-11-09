package com.tasksphere.shareme.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tasksphere.shareme.dto.UserInfo;
import com.tasksphere.shareme.entity.User;
import com.tasksphere.shareme.repository.UserRepository;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    public List<UserInfo> searchUsers(String query) {
        logger.info("Searching users with query: {}", query);
        try {
            List<User> users;
            if (query == null || query.trim().isEmpty()) {
                // Get all users and limit to 10
                List<User> allUsers = userRepository.findAll();
                users = allUsers.subList(0, Math.min(10, allUsers.size()));
                logger.info("No query provided, returning first {} users", users.size());
            } else {
                String searchQuery = query.trim();
                users = userRepository.findByEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                    searchQuery, searchQuery, searchQuery);
                logger.info("Found {} users matching query '{}'", users.size(), searchQuery);
            }
            
            List<UserInfo> userInfos = users.stream()
                .map(user -> new UserInfo(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getRole().toString()
                ))
                .collect(Collectors.toList());
            
            logger.info("Successfully converted {} users to UserInfo objects", userInfos.size());
            return userInfos;
            
        } catch (Exception e) {
            logger.error("Error searching users: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search users: " + e.getMessage(), e);
        }
    }
}