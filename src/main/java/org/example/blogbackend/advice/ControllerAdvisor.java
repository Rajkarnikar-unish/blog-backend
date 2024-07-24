package org.example.blogbackend.advice;

import org.example.blogbackend.exceptions.EmailAlreadyExistsException;
import org.example.blogbackend.exceptions.FileUploadFailureException;
import org.example.blogbackend.exceptions.PostNotFoundException;
import org.example.blogbackend.exceptions.UsernameAlreadyExistsException;
import org.example.blogbackend.models.Post;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class ControllerAdvisor extends ResponseEntityExceptionHandler {

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<Post> handlePostNotFound(PostNotFoundException exception, WebRequest request) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorMessage> handleUsernameAlreadyExistsException(UsernameAlreadyExistsException ex, WebRequest request) {
        ErrorMessage errorMessage = new ErrorMessage(
                HttpStatus.CONFLICT.value(),
                Instant.now().toEpochMilli(),
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorMessage, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorMessage> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex, WebRequest request) {
        ErrorMessage errorMessage = new ErrorMessage(
                HttpStatus.CONFLICT.value(),
                Instant.now().toEpochMilli(),
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorMessage, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(FileUploadFailureException.class)
    public ResponseEntity<ErrorMessage> handleFileUploadFailureException(FileUploadFailureException ex, WebRequest request) {
        ErrorMessage message = new ErrorMessage(
                HttpStatus.CONFLICT.value(),
                Instant.now().toEpochMilli(),
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(message, HttpStatus.CONFLICT);
    }

}
