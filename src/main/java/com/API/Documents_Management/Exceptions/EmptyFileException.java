package com.API.Documents_Management.Exceptions;


public class EmptyFileException extends RuntimeException {
    public EmptyFileException(String message) {
        super(message);
    }
}
