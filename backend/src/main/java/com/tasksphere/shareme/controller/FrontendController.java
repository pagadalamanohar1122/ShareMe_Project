package com.tasksphere.shareme.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {
    
    @GetMapping(value = {"/", "/login", "/signup", "/forgot-password", "/dashboard", "/projects", "/tasks"})
    public String forward() {
        return "forward:/index.html";
    }
}