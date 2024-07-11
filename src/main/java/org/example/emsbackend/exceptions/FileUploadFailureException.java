package org.example.emsbackend.exceptions;

public class FileUploadFailureException extends RuntimeException{

    public FileUploadFailureException(String message) {
        super(message);
    }
}
