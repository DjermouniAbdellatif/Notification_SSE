package com.API.Documents_Management.Exceptions;

public class AuthorityAlreadyExistsException extends RuntimeException {

    public AuthorityAlreadyExistsException(String message) {
        super(message);
    }

}