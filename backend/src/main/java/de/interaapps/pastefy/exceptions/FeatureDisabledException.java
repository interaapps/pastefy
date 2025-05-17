package de.interaapps.pastefy.exceptions;

public class FeatureDisabledException extends HTTPException {
    public FeatureDisabledException(String message) {
        super(403, message);
    }

    public FeatureDisabledException() {
        this("Feature disabled");
    }
}
