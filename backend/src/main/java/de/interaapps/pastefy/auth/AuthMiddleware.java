package de.interaapps.pastefy.auth;

import de.interaapps.pastefy.exceptions.AuthenticationException;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.handler.RequestHandler;

public class AuthMiddleware implements RequestHandler {
    public Object handle(Exchange exchange) {
        if (exchange.attrib("user") == null) {
            throw new AuthenticationException();
        }

        return null;
    }
}
