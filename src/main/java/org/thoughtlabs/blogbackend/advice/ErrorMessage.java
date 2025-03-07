package org.thoughtlabs.blogbackend.advice;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorMessage {
    private int statusCode;
    private LocalDateTime timestamp;
    private String message;
    private String description;

    public ErrorMessage(int statusCode, LocalDateTime timestamp, String message, String description) {
        this.statusCode = statusCode;
        this.timestamp = timestamp;
        this.message = message;
        this.description = description;
    }
}
