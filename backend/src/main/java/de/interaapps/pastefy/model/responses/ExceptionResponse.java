package de.interaapps.pastefy.model.responses;

public class ExceptionResponse extends ActionResponse {

    public String exception;
    public boolean error = true;
    public boolean exists = false;

    public ExceptionResponse(Throwable throwable) {
        throwable.printStackTrace();
        success = false;
        exception = throwable.getClass().getName();
    }
}
