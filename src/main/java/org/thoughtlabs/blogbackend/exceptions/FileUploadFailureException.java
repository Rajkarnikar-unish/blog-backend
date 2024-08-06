package org.thoughtlabs.blogbackend.exceptions;

public class FileUploadFailureException extends RuntimeException{

    public FileUploadFailureException(String message) {
        super(message);
    }
}
