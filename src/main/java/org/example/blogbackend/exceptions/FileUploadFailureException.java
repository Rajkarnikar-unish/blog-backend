package org.example.blogbackend.exceptions;

public class FileUploadFailureException extends RuntimeException{

    public FileUploadFailureException(String message) {
        super(message);
    }
}
