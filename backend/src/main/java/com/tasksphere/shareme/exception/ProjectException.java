package com.tasksphere.shareme.exception;

import org.springframework.http.HttpStatus;

public class ProjectException extends RuntimeException {
    private final HttpStatus status;

    public ProjectException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}