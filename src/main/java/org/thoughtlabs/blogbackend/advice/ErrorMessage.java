package org.thoughtlabs.blogbackend.advice;

import lombok.Getter;

@Getter
public class ErrorMessage {
    private int statusCode;
    private long timestamp;
    private String message;
    private String description;

    public ErrorMessage(int statusCode, long timestamp, String message, String description) {
        this.statusCode = statusCode;
        this.timestamp = timestamp;
        this.message = message;
        this.description = description;
    }
}
