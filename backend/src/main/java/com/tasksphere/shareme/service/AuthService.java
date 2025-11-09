package com.tasksphere.shareme.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tasksphere.shareme.dto.AuthResponse;
import com.tasksphere.shareme.dto.ForgotPasswordRequest;
import com.tasksphere.shareme.dto.LoginRequest;
import com.tasksphere.shareme.dto.ResetPasswordRequest;
import com.tasksphere.shareme.dto.SignupRequest;
import com.tasksphere.shareme.dto.UserInfo;
import com.tasksphere.shareme.entity.PasswordReset;
import com.tasksphere.shareme.entity.User;
import com.tasksphere.shareme.repository.PasswordResetRepository;
import com.tasksphere.shareme.repository.UserRepository;
import com.tasksphere.shareme.security.JwtTokenProvider;

@Service
@Transactional
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordResetRepository passwordResetRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        
        User user = new User(
            request.getFirstName(),
            request.getLastName(),
            request.getEmail(),
            passwordEncoder.encode(request.getPassword())
        );
        
        userRepository.save(user);
        
        // Note: User needs to login separately after signup
        // This is a more professional approach than auto-login
    }
    
    public AuthResponse login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        
        if (userOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOpt.get().getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        
        User user = userOpt.get();
        
        String accessToken = tokenProvider.generateAccessToken(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole().name()
        );
        
        UserInfo userInfo = new UserInfo(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getRole().name()
        );
        
        return new AuthResponse(accessToken, userInfo);
    }
    
    public UserInfo getCurrentUser(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email cannot be empty");
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        
        User user = userOpt.get();
        return new UserInfo(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getRole().name()
        );
    }
    
    public void forgotPassword(ForgotPasswordRequest request) {
        // Always return success to prevent user enumeration
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Generate secure random token
            byte[] tokenBytes = new byte[32];
            secureRandom.nextBytes(tokenBytes);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
            
            // Hash the token before storing
            String tokenHash = hashToken(token);
            
            // Create password reset record with 15 minute expiry
            PasswordReset passwordReset = new PasswordReset(
                user.getId(),
                tokenHash,
                LocalDateTime.now().plusMinutes(15)
            );
            
            passwordResetRepository.save(passwordReset);
            
            // TODO: Send email with reset link containing the original token
            // For now, just log the token (in production, this should be sent via email)
            System.out.println("Password reset token for " + request.getEmail() + ": " + token);
            System.out.println("Reset link: http://localhost:3000/reset?token=" + token);
        }
    }
    
    public void resetPassword(ResetPasswordRequest request) {
        String tokenHash = hashToken(request.getToken());
        
        Optional<PasswordReset> resetOpt = passwordResetRepository.findByTokenHashAndUsedFalse(tokenHash);
        
        if (resetOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
        
        PasswordReset passwordReset = resetOpt.get();
        
        if (passwordReset.isExpired()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
        
        Long userId = passwordReset.getUserId();
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        
        User user = userOpt.get();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        // Mark token as used
        passwordResetRepository.markTokenAsUsed(tokenHash);
    }
    
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}