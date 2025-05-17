package de.interaapps.pastefy.exceptions;

public class PastePrivateException extends HTTPException {
    public PastePrivateException(String message) {
        super(403, message);
    }

    public PastePrivateException() {
        this("Paste is private");
    }
}
