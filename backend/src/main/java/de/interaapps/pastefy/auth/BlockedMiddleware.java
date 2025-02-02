package de.interaapps.pastefy.auth;

import de.interaapps.pastefy.exceptions.BlockedException;
import de.interaapps.pastefy.model.database.User;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.handler.Middleware;

public class BlockedMiddleware implements Middleware {
    @Override
    public Object handle(Exchange exchange) {
        User user = exchange.attrib("user");

        if (user != null && user.type == User.Type.BLOCKED) {
            throw new BlockedException();
        }

        return null;
    }
}
