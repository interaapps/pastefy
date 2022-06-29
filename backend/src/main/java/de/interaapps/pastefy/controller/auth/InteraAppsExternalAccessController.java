package de.interaapps.pastefy.controller.auth;

import de.interaapps.pastefy.Pastefy;
import de.interaapps.pastefy.controller.HttpController;
import de.interaapps.pastefy.exceptions.AuthenticationException;
import de.interaapps.pastefy.exceptions.NotFoundException;
import de.interaapps.pastefy.model.database.AuthKey;
import de.interaapps.pastefy.model.database.User;
import de.interaapps.pastefy.model.requests.auth.InteraAppsExternalAccessRequest;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.httpserver.router.annotation.params.Body;
import org.javawebstack.httpserver.router.annotation.verbs.Post;
import org.javawebstack.orm.Repo;
import org.javawebstack.passport.strategies.oauth2.OAuth2Provider;
import org.javawebstack.webutils.config.Config;

import java.util.Map;

@PathPrefix("/api/v2/auth")
public class InteraAppsExternalAccessController extends HttpController {
    @Post("/iaea")
    public String iaea(@Body InteraAppsExternalAccessRequest request, Exchange exchange) {
        Map<String, OAuth2Provider> providers = Pastefy.getInstance().getOAuth2Strategy().getProviders();

        if (providers.containsKey("interaapps")) {

            Config config = Pastefy.getInstance().getConfig();

            if (config.get("oauth2.interaapps.id").equals(request.appId) && config.get("oauth2.interaapps.secret").equals(request.appSecret)) {
                User user = Repo.get(User.class).where("authId", request.userId).where("authProvider", User.AuthenticationProvider.INTERAAPPS).first();

                if (user != null) {
                    AuthKey authKey = new AuthKey();
                    authKey.userId = user.id;
                    authKey.type = AuthKey.Type.ACCESS_TOKEN;
                    request.appScopeList.forEach(authKey::addScope);

                    authKey.save();
                    return authKey.getKey();
                }
            } else {
                throw new AuthenticationException();
            }
        }

        throw new NotFoundException();
    }
}
