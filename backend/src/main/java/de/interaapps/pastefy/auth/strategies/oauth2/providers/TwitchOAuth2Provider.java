package org.javawebstack.passport.strategies.oauth2.providers;

import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.abstractdata.util.QueryString;
import org.javawebstack.http.router.util.MimeType;
import org.javawebstack.httpclient.HTTPClient;
import org.javawebstack.passport.strategies.oauth2.OAuth2Profile;
import org.javawebstack.passport.strategies.oauth2.OAuth2Provider;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class TwitchOAuth2Provider implements OAuth2Provider {
    private String clientId;
    private String secret;
    private String[] scopes = {"user:read:email"}; // reference: https://dev.twitch.tv/docs/authentication/#scopes

    private boolean forceVerify = false;
    private String state;

    public TwitchOAuth2Provider(String clientId, String secret){
        this.clientId = clientId;
        this.secret = secret;
    }

    public TwitchOAuth2Provider setScopes(String... scopes) {
        this.scopes = scopes;
        return this;
    }

    public OAuth2Callback callback(AbstractObject queryParameters, String callbackUrl) {
        AbstractObject abstractObject = new HTTPClient("https://id.twitch.tv").post("/oauth2/token")
                .formBodyString(new QueryString()
                        .set("client_id", clientId)
                        .set("client_secret", secret)
                        .set("code", queryParameters.string("code"))
                        .set("grant_type", "authorization_code")
                        .set("redirect_uri", callbackUrl)
                        .toString())
                .header("Accept", MimeType.JSON.getMimeTypes().get(0))
                .data().object();

        if (abstractObject.has("scope")) {
            return new OAuth2Callback(this, abstractObject.string("access_token"), abstractObject.string("refresh_token"), clientId);
        }

        return null;
    }

    public TwitchOAuth2Provider setState(String state) {
        this.state = state;
        return this;
    }

    public String getState() {
        return state;
    }

    public TwitchOAuth2Provider setForceVerify(boolean forceVerify) {
        this.forceVerify = forceVerify;
        return this;
    }

    public boolean isForceVerify() {
        return forceVerify;
    }

    public String redirect(String callbackUrl) {
        try {
            return "https://id.twitch.tv/oauth2/authorize?client_id="+clientId+"&response_type=code&scope="+ URLEncoder.encode(String.join(" ", scopes), "UTF-8")+"&redirect_uri="+URLEncoder.encode(callbackUrl, "UTF-8")+"&force_verify="+forceVerify+"&state="+state;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static class OAuth2Callback extends org.javawebstack.passport.strategies.oauth2.OAuth2Callback {
        private String clientId;

        public OAuth2Callback(OAuth2Provider provider, String accessToken, String refreshToken, String clientId) {
            super(provider, accessToken, refreshToken);
            this.clientId = clientId;
        }

        public HTTPClient createApiClient() {
            return new HTTPClient("https://api.twitch.tv/helix").bearer(accessToken);
        }

        public OAuth2Profile getProfile() {
            OAuth2Profile profile = new OAuth2Profile();

            AbstractObject userData = createApiClient().get("/users")
                    .header("Client-Id", clientId)
                    .data().object().get("data").array().get(0).object();

            if (userData.has("id"))
                profile.id = userData.get("id").string();
            if (userData.has("login"))
                profile.name = userData.get("login").string();
            if (userData.has("profile_image_url"))
                profile.avatar = userData.get("profile_image_url").string();
            if (userData.has("email"))
                profile.mail = userData.get("email").string();
            userData.forEach(profile::set);

            return profile;
        }
    }

}
