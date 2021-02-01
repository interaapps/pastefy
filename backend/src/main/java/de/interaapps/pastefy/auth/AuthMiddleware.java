package de.interaapps.pastefy.auth;

import de.interaapps.pastefy.exceptions.AuthenticationException;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.handler.RequestHandler;

public class AuthMiddleware implements RequestHandler {
    public Object handle(Exchange exchange) {
        if (exchange.attrib("user") == null)
            throw new AuthenticationException();

        return null;
    }
}
