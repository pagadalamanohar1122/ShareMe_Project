package com.tasksphere.shareme.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tasksphere.shareme.dto.ErrorResponse;
import com.tasksphere.shareme.dto.UserInfo;
import com.tasksphere.shareme.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Search and basic user lookup")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Search users by name or email")
    @CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
    public ResponseEntity<?> searchUsers(@RequestParam(required = false) String q) {
        try {
            logger.info("Received user search request with query: {}", q);
            List<UserInfo> users = userService.searchUsers(q);
            logger.info("Found {} users matching the search criteria", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error searching users: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500, "Failed to search users: " + e.getMessage(), e.getClass().getSimpleName()));
        }
    }
}