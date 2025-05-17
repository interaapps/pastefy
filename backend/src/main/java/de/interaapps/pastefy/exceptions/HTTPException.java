package de.interaapps.pastefy.exceptions;

public class HTTPException  extends RuntimeException {
    public int status = 500;

    public HTTPException(int status, String message) {
        super(message);
        this.status = status;
    }
}
