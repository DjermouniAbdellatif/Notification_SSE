package com.API.Documents_Management.Exceptions;

public class AuthorityNotFoundException extends RuntimeException {
    public AuthorityNotFoundException(String message) {
        super(message);
    }
}