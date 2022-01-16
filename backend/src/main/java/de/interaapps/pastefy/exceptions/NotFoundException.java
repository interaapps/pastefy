package de.interaapps.pastefy.exceptions;

public class NotFoundException extends RuntimeException {
    public NotFoundException(){
        super("Resource not found");
    }
}
