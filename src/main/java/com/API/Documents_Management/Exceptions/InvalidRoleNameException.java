package com.API.Documents_Management.Exceptions;

public class InvalidRoleNameException extends RuntimeException {

    public InvalidRoleNameException(String message) {
        super(message);
    }

}