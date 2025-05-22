package com.API.Documents_Management.Exceptions;


    public class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
}
