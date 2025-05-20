package com.API.Documents_Management.Config;

import com.API.Documents_Management.Dto.ApiResponse;
import com.API.Documents_Management.Exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(ex.getMessage(), null));
    }


    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ApiResponse<String>> handleAlreadyExists(AlreadyExistsException ex) {


        ApiResponse<String> body = ApiResponse.<String>builder()
                .message(ex.getMessage())
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }




    @ExceptionHandler({ FileTypeNotAllowedException.class,
            FileTooLargeException.class,
            EmptyFileException.class })
    public ResponseEntity<ApiResponse<String>> handleFileErrors(RuntimeException ex) {
        ApiResponse<String> body = ApiResponse.<String>builder()
                .message(ex.getMessage())
                .data(null)
                .build();
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler({ FileNotFoundException.class})
    public ResponseEntity<ApiResponse<String>> handleFileNotFoundException(RuntimeException ex) {
        ApiResponse<String> body = ApiResponse.<String>builder()
                .message(ex.getMessage())
                .data(null)
                .build();
        return ResponseEntity.badRequest().body(body);
    }




    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleNotFound(ResourceNotFoundException ex) {
        ApiResponse<String> body = ApiResponse.<String>builder()
                .message(ex.getMessage())
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }



    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGeneral(Exception ex) {
        ApiResponse<String> body = ApiResponse.<String>builder()
                .message("Internal error!")
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
