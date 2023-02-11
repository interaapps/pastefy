package de.interaapps.pastefy.model.responses;

import de.interaapps.pastefy.exceptions.AuthenticationException;
import de.interaapps.pastefy.exceptions.NotFoundException;

public class ExceptionResponse extends ActionResponse {

    public String exception;
    public boolean error = true;
    public boolean exists = false;

    public ExceptionResponse(Throwable throwable) {
        if (!(throwable instanceof AuthenticationException || throwable instanceof NotFoundException))
            throwable.printStackTrace();
        success = false;

        exception = throwable.getClass().getSimpleName();
    }
}
