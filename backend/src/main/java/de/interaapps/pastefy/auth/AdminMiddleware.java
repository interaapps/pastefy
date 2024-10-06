package de.interaapps.pastefy.auth;

import de.interaapps.pastefy.exceptions.PermissionsDeniedException;
import de.interaapps.pastefy.model.database.User;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.handler.RequestHandler;

public class AdminMiddleware implements RequestHandler {
    public Object handle(Exchange exchange) {

        new AuthMiddleware().handle(exchange);

        User user = exchange.attrib("user");

        if (user.type != User.Type.ADMIN) {
            throw new PermissionsDeniedException();
        }

        return null;
    }
}
