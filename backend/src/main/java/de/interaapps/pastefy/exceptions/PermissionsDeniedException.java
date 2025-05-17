package de.interaapps.pastefy.exceptions;

public class PermissionsDeniedException extends HTTPException {
    public PermissionsDeniedException(String message) {
        super(403, message);
    }

    public PermissionsDeniedException() {
        this("Permission denied");
    }
}
