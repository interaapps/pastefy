package de.interaapps.pastefy.exceptions;

import com.google.api.client.http.HttpStatusCodes;

public class BlockedException extends HTTPException {
    public BlockedException(String message) {
        super(HttpStatusCodes.STATUS_CODE_UNAUTHORIZED, message);
    }

    public BlockedException() {
        this("Blocked");
    }
}
