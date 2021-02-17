package de.interaapps.pastefy.controller.user.auth;

import de.interaapps.pastefy.Pastefy;
import de.interaapps.pastefy.auth.AuthenticationProvider;
import org.javawebstack.framework.HttpController;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.router.annotation.Get;
import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.injector.Inject;

@PathPrefix("/api/authentication")
public class AuthController extends HttpController {

    @Inject
    public AuthenticationProvider authenticationProvider;

    @Get("/login")
    public String login(Exchange exchange) {
        if (exchange.rawRequest().getParameter("userkey") != null) {
            String key = authenticationProvider.login(exchange.rawRequest().getParameter("userkey"));

            if (key != null)
                exchange.redirect("/auth?key=" + key);
        } else {
            exchange.redirect("https://accounts.interaapps.de/iaauth/" + Pastefy.getInstance().getConfig().get("interaapps.auth.id"));
        }
        return "";
    }

}