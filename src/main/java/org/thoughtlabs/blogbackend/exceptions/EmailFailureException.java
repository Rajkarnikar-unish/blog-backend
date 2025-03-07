package org.thoughtlabs.blogbackend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class EmailFailureException extends Exception {

    public String message;

    public EmailFailureException(String message) {
        super(message);
    }

    public EmailFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
