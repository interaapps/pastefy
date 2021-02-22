package de.interaapps.pastefy.model.responses;

import de.interaapps.pastefy.exceptions.AuthenticationException;

public class ExceptionResponse extends ActionResponse {

    public String exception;
    public boolean error = true;
    public boolean exists = false;

    public ExceptionResponse(Throwable throwable) {
        if (throwable instanceof AuthenticationException)
            System.err.println("AUTH EXCEPTION");
        else
            throwable.printStackTrace();
        success = false;
        exception = throwable.getClass().getName();
    }
}
