package de.interaapps.pastefy.model.responses;

public class ExceptionResponse extends ActionResponse {

    public String exception;
    public boolean exists = false;

    public ExceptionResponse(Throwable throwable){
        success = false;
        exception = throwable.getClass().getName();
    }
}
