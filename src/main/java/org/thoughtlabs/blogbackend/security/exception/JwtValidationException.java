package org.thoughtlabs.blogbackend.security.exception;

public class JwtValidationException extends Exception{
    public JwtValidationException(String message) {
        super(message);
    }
}
