/*
 * HomeController
 * - Provides a safe JSON response for GET "/".
 * - Avoids 500 by handling root explicitly.
 * - Also exposes /home that redirects to Swagger UI.
 */
package com.tasksphere.shareme.web;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@Hidden // Hide infrastructure/root endpoints from Swagger UI to keep user flow focused
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("app", "ShareMe API");
        body.put("status", "OK");
        body.put("time", OffsetDateTime.now().toString());
        body.put("swagger", "/swagger-ui/index.html");
        return ResponseEntity.ok(body);
    }

    @GetMapping("/home")
    public RedirectView homeToSwagger() {
        return new RedirectView("/swagger-ui/index.html");
    }
}