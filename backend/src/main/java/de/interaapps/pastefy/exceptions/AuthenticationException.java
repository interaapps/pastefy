package de.interaapps.pastefy.exceptions;

public class AuthenticationException extends HTTPException {

    public AuthenticationException(String message) {
        super(401, message);
    }

    public AuthenticationException() {
        this("Not authenticated");
    }
}
