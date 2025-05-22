package com.API.Documents_Management.Config;

import com.API.Documents_Management.Dto.ApiResponse;
import com.API.Documents_Management.Exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 🔹 Illegal arguments
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.builder()
                        .message(ex.getMessage())
                        .data(null)
                        .build());
    }

    // 🔹 Duplicate / Conflict errors
    @ExceptionHandler({
            AlreadyExistsException.class,
            UserAlreadyExistsException.class
    })
    public ResponseEntity<ApiResponse<String>> handleAlreadyExists(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.<String>builder()
                        .message(ex.getMessage())
                        .data(null)
                        .build());
    }

    // 🔹 Not Found errors
    @ExceptionHandler({
            ResourceNotFoundException.class,
            FileNotFoundException.class,
            UserNotFoundException.class,
            RoleNotFoundException.class,
            AuthorityNotFoundException.class
    })
    public ResponseEntity<ApiResponse<String>> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.<String>builder()
                        .message(ex.getMessage())
                        .data(null)
                        .build());
    }

    // 🔹 File validation errors
    @ExceptionHandler({
            FileTypeNotAllowedException.class,
            FileTooLargeException.class,
            EmptyFileException.class
    })
    public ResponseEntity<ApiResponse<String>> handleFileValidationErrors(RuntimeException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.<String>builder()
                        .message(ex.getMessage())
                        .data(null)
                        .build());
    }

    // 🔹 Invalid name (role/authority)
    @ExceptionHandler({
            InvalidRoleNameException.class,
            InvalidAuthorityNameException.class
    })
    public ResponseEntity<ApiResponse<String>> handleInvalidName(RuntimeException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.<String>builder()
                        .message(ex.getMessage())
                        .data(null)
                        .build());
    }

    // 🔹 Validation errors (from DTOs with @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest()
                .body(ApiResponse.<String>builder()
                        .message(errors)
                        .data(null)
                        .build());
    }

    // 🔹 Default fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<String>builder()
                        .message("Internal server error: " + ex.getMessage())
                        .data(null)
                        .build());
    }
}
