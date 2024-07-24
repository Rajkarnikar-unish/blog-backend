package org.example.blogbackend.security.exception;

public class JwtValidationException extends Exception{
    public JwtValidationException(String message) {
        super(message);
    }
}
