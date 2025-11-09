package com.tasksphere.shareme.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTestUtil {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String plainPassword = "password123";
        
        // Generate a new correct hash
        String correctHash = encoder.encode(plainPassword);
        System.out.println("Correct BCrypt hash for 'password123': " + correctHash);
        
        // Verify it works
        boolean matches = encoder.matches(plainPassword, correctHash);
        System.out.println("New hash matches: " + matches);
    }
}