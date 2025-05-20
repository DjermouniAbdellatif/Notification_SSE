package com.API.Documents_Management.Exceptions;


public class FileTypeNotAllowedException extends RuntimeException {
    public FileTypeNotAllowedException(String message) { super(message); }
}