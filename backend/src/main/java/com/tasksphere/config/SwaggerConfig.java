package com.tasksphere.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI taskSphereOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ShareMe TaskSphere API")
                        .description("""
                                # Professional Task Management System API
                                
                                Complete API documentation for ShareMe TaskSphere - An enterprise-grade task management platform.
                                
                                ## Features:
                                - **Authentication & Authorization**: JWT-based secure authentication
                                - **Task Management**: Full CRUD operations with advanced filtering and search
                                - **Project Management**: Organize tasks within projects with role-based access
                                - **Personal Notes**: Task-specific personal notes and reminder tags
                                - **File Attachments**: Upload, download, and manage task attachments
                                - **Real-time Updates**: Live task status updates and notifications
                                - **Advanced Search**: Multi-criteria search and filtering capabilities
                                
                                ## API Standards:
                                - RESTful design principles
                                - Consistent error handling
                                - Comprehensive input validation
                                - Secure by default (Bearer token required)
                                - Standardized response formats
                                
                                ## Getting Started:
                                1. Register a new account using `/api/auth/signup`
                                2. Login to get your JWT token via `/api/auth/login`
                                3. Include the token in Authorization header: `Bearer <your-token>`
                                4. Start managing your projects and tasks!
                                
                                ## Support:
                                For technical support, contact our development team or visit our documentation portal.
                                """)
                        .version("v2.0.0")
                        .contact(new Contact()
                                .name("ShareMe TaskSphere Development Team")
                                .email("api-support@shareme-tasksphere.com")
                                .url("https://docs.shareme-tasksphere.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.shareme-tasksphere.com")
                                .description("Production Server"),
                        new Server()
                                .url("https://staging-api.shareme-tasksphere.com")
                                .description("Staging Server")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer")
                .description("""
                        Enter JWT Bearer token obtained from `/api/auth/login` endpoint.
                        
                        **Format**: Bearer <your-jwt-token>
                        
                        **Example**: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
                        
                        **Note**: The token will automatically expire after 15 minutes for security.
                        """);
    }
}