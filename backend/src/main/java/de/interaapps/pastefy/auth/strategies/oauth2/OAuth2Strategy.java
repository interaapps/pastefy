package de.interaapps.pastefy.auth.strategies.oauth2;

import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.HTTPRouter;
import de.interaapps.pastefy.auth.strategies.Strategy;

import java.util.HashMap;
import java.util.Map;

public class OAuth2Strategy extends Strategy {

    private Map<String, OAuth2Provider> providers = new HashMap<>();
    private HttpCallbackHandler httpCallbackHandler;
    private String host;


    public OAuth2Strategy(String host){

        this.host = host;
    }

    public OAuth2Strategy use(String name, OAuth2Provider oAuth2Provider){
        providers.put(name, oAuth2Provider);
        return this;
    }

    public void createRoutes(String prefixUrl, HTTPRouter httpRouter) {
        providers.forEach((name, oauth2) -> {
            final String callbackUrl = prefixUrl+"/"+name+"/callback";
            httpRouter.get(prefixUrl+"/"+name, e -> {
                e.redirect(oauth2.redirect(host+callbackUrl));
                return "";
            });

            httpRouter.get(callbackUrl, e -> {
                return httpCallbackHandler.handle(e, oauth2.callback(e.getQueryParameters(), host+callbackUrl), name);
            });
        });
    }

    public Map<String, OAuth2Provider> getProviders() {
        return providers;
    }

    public void setHttpCallbackHandler(HttpCallbackHandler httpCallbackHandler) {
        this.httpCallbackHandler = httpCallbackHandler;
    }

    public interface HttpCallbackHandler {
        Object handle(Exchange exchange, OAuth2Callback callback, String provider);
    }
}
