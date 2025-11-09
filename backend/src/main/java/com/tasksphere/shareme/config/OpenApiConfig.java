package com.tasksphere.shareme.config;

import java.util.List;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("ShareMe TaskSphere API")
                .description("""
                    # Getting Started Flow
                    1. Sign up (`POST /api/auth/signup`) or login (`POST /api/auth/login`). Copy the `token` field from the login response.
                    2. Click "Authorize" and paste the token as: `Bearer <JWT>` (prefix required).
                    3. Create or list Projects (`/api/projects`). Projects are the container for Tasks.
                    4. Create Tasks inside a Project (`/api/tasks`). Then filter/search tasks with rich query params.
                    5. Add personal Task Notes (`/api/task-notes`) to track reminders & context per task.
                    6. Upload / manage Attachments (`/api/tasks/{taskId}/attachments`).
                    7. Use stats endpoints (`/api/projects/stats`, `/api/tasks/stats`) for dashboard summaries.

                    Public endpoints under `/api/auth/**` intentionally have no security requirement so you can try them first.
                    All other domains require the JWT bearer token.
                    """.stripIndent())
                .version("v2.2.0")
                .contact(new Contact().name("ShareMe Team").email("support@shareme.local"))
                .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Local development")
            ))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Supply a valid JWT obtained from /api/auth/login"))
            );
    }

    // Grouped APIs to keep Swagger clean and focused
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
            .group("Authentication")
            .pathsToMatch("/api/auth/**")
            .build();
    }

    @Bean
    public GroupedOpenApi usersApi() {
        return GroupedOpenApi.builder()
            .group("Users")
            .pathsToMatch("/api/users/**")
            .build();
    }

    @Bean
    public GroupedOpenApi projectsApi() {
        return GroupedOpenApi.builder()
            .group("Projects")
            .pathsToMatch("/api/projects/**")
            .build();
    }

    @Bean
    public GroupedOpenApi tasksApi() {
        return GroupedOpenApi.builder()
            .group("Tasks")
            .pathsToMatch("/api/tasks/**")
            .build();
    }

    @Bean
    public GroupedOpenApi taskNotesApi() {
        return GroupedOpenApi.builder()
            .group("Task Notes")
            .pathsToMatch("/api/task-notes/**")
            .build();
    }

    @Bean
    public GroupedOpenApi taskAttachmentsApi() {
        return GroupedOpenApi.builder()
            .group("Task Attachments")
            // Match nested attachment routes under tasks
            .pathsToMatch(
                "/api/tasks/**/attachments",
                "/api/tasks/**/attachments/**",
                "/api/tasks/attachments/**"
            )
            .build();
    }

    // Remove security requirement from public endpoints (/api/auth/**)
    @Bean
    public OpenApiCustomizer publicEndpointsWithoutSecurity() {
        return openApi -> {
            if (openApi.getPaths() == null) return;
            openApi.getPaths().forEach((path, pathItem) -> {
                if (StringUtils.hasText(path) && path.startsWith("/api/auth")) {
                    pathItem.readOperations().forEach(op -> clearSecurity(op));
                }
            });
        };
    }

    private void clearSecurity(Operation operation) {
        if (operation != null) {
            operation.setSecurity(null);
        }
    }
}