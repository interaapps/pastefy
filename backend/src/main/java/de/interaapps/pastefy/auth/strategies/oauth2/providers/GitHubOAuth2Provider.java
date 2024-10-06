package de.interaapps.pastefy.auth.strategies.oauth2.providers;

import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.abstractdata.util.QueryString;
import org.javawebstack.http.router.util.MimeType;
import org.javawebstack.httpclient.HTTPClient;
import de.interaapps.pastefy.auth.strategies.oauth2.OAuth2Profile;
import de.interaapps.pastefy.auth.strategies.oauth2.OAuth2Provider;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class GitHubOAuth2Provider implements OAuth2Provider {
    private String clientId;
    private String secret;
    private String[] scopes = {"read:user","user:email"};
    public GitHubOAuth2Provider(String clientId, String secret){
        this.clientId = clientId;
        this.secret = secret;
    }

    public GitHubOAuth2Provider setScopes(String... scopes) {
        this.scopes = scopes;
        return this;
    }

    public OAuth2Callback callback(AbstractObject queryParameters, String callbackUrl) {
        AbstractObject abstractObject = new HTTPClient("https://github.com").post("/login/oauth/access_token")
                .formBodyString(new QueryString()
                        .set("client_id", clientId)
                        .set("client_secret", secret)
                        .set("code", queryParameters.string("code"))
                        .toString())
                .header("Accept", MimeType.JSON.getMimeTypes().get(0))
                .data().object();


        if (abstractObject.has("scope")/* && abstractObject.get("scope").string().equals("read:user,user:email")*/) {
            return new OAuth2Callback(this, abstractObject.string("access_token"), abstractObject.has("refresh_token") ? abstractObject.string("refresh_token") : null);
        }

        return null;
    }

    public String redirect(String callbackUrl) {
        try {
            return "https://github.com/login/oauth/authorize?client_id="+clientId+"&scope="+ URLEncoder.encode(String.join(" ", scopes), "UTF-8")+"&redirect_uri="+URLEncoder.encode(callbackUrl, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static class OAuth2Callback extends de.interaapps.pastefy.auth.strategies.oauth2.OAuth2Callback {
        public OAuth2Callback(OAuth2Provider provider, String accessToken, String refreshToken) {
            super(provider, accessToken, refreshToken);
        }

        public HTTPClient createApiClient() {
            return new HTTPClient("https://api.github.com").authorization("token", accessToken);
        }

        public OAuth2Profile getProfile() {
            HTTPClient apiClient = createApiClient();
            OAuth2Profile profile = new OAuth2Profile();

            AbstractObject userData = apiClient.get("/user")
                    .header("Authorization", "token "+accessToken)
                    .data().object();

            if (userData.has("id"))
                profile.id = userData.get("id").number().toString();
            if (userData.has("name"))
                profile.name = userData.get("name").string();
            if (userData.has("avatar_url"))
                profile.avatar = userData.get("avatar_url").string();

            userData.forEach(profile::set);

            apiClient.get("/user/emails")
                    .authorization("token", accessToken)
                    .data().array().forEach(abstractElement -> {
                        if (profile.mail == null) {
                            profile.mail = abstractElement.object().get("email").string();
                        }
                    });
            return profile;
        }
    }
}
