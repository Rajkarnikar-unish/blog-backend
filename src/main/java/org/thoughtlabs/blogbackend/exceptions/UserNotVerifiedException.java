package org.thoughtlabs.blogbackend.exceptions;

public class UserNotVerifiedException extends Exception{

    private boolean newEmailSent;
    private String message;

    public UserNotVerifiedException(boolean newEmailSent, String message) {
        this.newEmailSent = newEmailSent;
        this.message = message;
    }

    public boolean isNewEmailSent() {
        return newEmailSent;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
