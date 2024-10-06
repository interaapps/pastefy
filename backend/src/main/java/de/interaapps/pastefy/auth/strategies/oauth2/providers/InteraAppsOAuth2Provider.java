package org.javawebstack.passport.strategies.oauth2.providers;

import org.javawebstack.abstractdata.AbstractElement;
import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.httpclient.HTTPClient;
import org.javawebstack.passport.strategies.oauth2.OAuth2Profile;
import org.javawebstack.passport.strategies.oauth2.OAuth2Provider;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

public class InteraAppsOAuth2Provider implements OAuth2Provider {
    private String clientId;
    private String secret;
    private String[] scopes = {"user:read"};
    private HTTPClient interaAppsAccountsClient;

    public InteraAppsOAuth2Provider(String clientId, String secret){
        this.clientId = clientId;
        this.secret = secret;
        interaAppsAccountsClient = new HTTPClient("https://accounts.interaapps.de/api/v2");
    }

    public InteraAppsOAuth2Provider setScopes(String... scopes) {
        this.scopes = scopes;
        return this;
    }

    public OAuth2Callback callback(AbstractObject queryParameters, String callbackUrl) {
        AbstractObject data = interaAppsAccountsClient.post("/authorization/oauth2/access_token")
                .jsonBodyElement(new AbstractObject()
                        .set("client_id", clientId)
                        .set("client_secret", secret)
                        .set("code", queryParameters.string("code"))
                )
                .data()
                .object();

        List<String> scopes = Arrays.asList(this.scopes);

        if (!data.bool("success"))
            return null;

        for (AbstractElement scope : data.get("scope_list").array()) {
            if (!scopes.contains(scope.string()))
                return null;
        }

        if (data.get("success").bool()) {
            return new OAuth2Callback(this, data.string("access_token"), data.string("refresh_token"));
        }

        return null;
    }

    public String redirect(String callbackUrl) {
        try {
            return "https://accounts.interaapps.de/auth/oauth2?client_id="+clientId+"&scope="+ URLEncoder.encode(String.join(" ", scopes), "UTF-8")+"&redirect_uri="+URLEncoder.encode(callbackUrl, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static class OAuth2Callback extends org.javawebstack.passport.strategies.oauth2.OAuth2Callback {
        public OAuth2Callback(OAuth2Provider provider, String accessToken, String refreshToken) {
            super(provider, accessToken, refreshToken);
        }

        public HTTPClient createApiClient() {
            return new HTTPClient("https://accounts.interaapps.de/api/v2").bearer(accessToken);
        }

        public OAuth2Profile getProfile() {
            AbstractObject userData = createApiClient().get("/user").data().object();
            OAuth2Profile profile = new OAuth2Profile();

            if (userData.has("id"))
                profile.id = userData.get("id").number().toString();
            if (userData.has("name"))
                profile.name = userData.get("name").string();
            if (userData.has("mail"))
                profile.mail = userData.get("mail").string();
            if (userData.has("profile_picture"))
                profile.avatar = userData.get("profile_picture").string();

            userData.forEach(profile::set);
            return profile;
        }

    }
}
