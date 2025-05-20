package com.API.Documents_Management.Exceptions;


public class FileTooLargeException extends RuntimeException {
    public FileTooLargeException(String message) { super(message); }
}