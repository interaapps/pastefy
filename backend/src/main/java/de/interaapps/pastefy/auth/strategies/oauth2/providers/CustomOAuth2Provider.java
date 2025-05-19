package de.interaapps.pastefy.auth.strategies.oauth2.providers;

import de.interaapps.pastefy.auth.strategies.oauth2.OAuth2Profile;
import de.interaapps.pastefy.auth.strategies.oauth2.OAuth2Provider;
import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.abstractdata.util.QueryString;
import org.javawebstack.httpclient.HTTPClient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class CustomOAuth2Provider implements OAuth2Provider {
    private final String clientId;
    private final String secret;
    private final String authEndpoint;
    private final String tokenEndpoint;
    private final String userInfoEndpoint;
    private String[] scopes = {"openid", "email", "profile"};

    public CustomOAuth2Provider(String clientId, String secret, String authEndpoint, String tokenEndpoint, String userInfoEndpoint) {
        this.clientId = clientId;
        this.secret = secret;
        this.authEndpoint = authEndpoint;
        this.tokenEndpoint = tokenEndpoint;
        this.userInfoEndpoint = userInfoEndpoint;
    }

    public CustomOAuth2Provider setScopes(String... scopes) {
        this.scopes = scopes;
        return this;
    }

    public OAuth2Callback callback(AbstractObject queryParameters, String callbackUrl) {
        HTTPClient httpClient = new HTTPClient();

        AbstractObject tokenData = httpClient.post(tokenEndpoint)
                .header("User-Agent", "PastefyOIDCClient/1")
                .formBodyString(new QueryString()
                        .set("client_id", clientId)
                        .set("client_secret", secret)
                        .set("code", queryParameters.string("code"))
                        .set("grant_type", "authorization_code")
                        .set("redirect_uri", callbackUrl)
                        .set("scope", String.join(" ", scopes))
                        .toString())
                .data().object();

        return new OAuth2Callback(this, tokenData.string("access_token"), tokenData.string("refresh_token"), userInfoEndpoint);
    }

    public String redirect(String callbackUrl) {
        return authEndpoint +
                "?response_type=code" +
                "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                "&redirect_uri=" + URLEncoder.encode(callbackUrl, StandardCharsets.UTF_8) +
                "&scope=" + URLEncoder.encode(String.join(" ", scopes), StandardCharsets.UTF_8) +
                "&prompt=consent";
    }


    public static class OAuth2Callback extends de.interaapps.pastefy.auth.strategies.oauth2.OAuth2Callback {
        private final String userInfoEndpoint;

        public OAuth2Callback(OAuth2Provider provider, String accessToken, String refreshToken, String userInfoEndpoint) {
            super(provider, accessToken, refreshToken);
            this.userInfoEndpoint = userInfoEndpoint;
        }

        public HTTPClient createApiClient() {
            return new HTTPClient().bearer(accessToken);
        }

        public OAuth2Profile getProfile() {
            AbstractObject data = createApiClient()
                    .get(userInfoEndpoint)
                    .header("User-Agent", "PastefyOIDCClient/1")
                    .data().object();

            OAuth2Profile profile = new OAuth2Profile();

            profile.id = data.get("sub").string();
            profile.mail = data.get("email").string();
            profile.name = data.get("name").string();

            if (data.has("picture"))
                profile.avatar = data.get("picture").string();

            data.forEach(profile::set);

            return profile;
        }
    }
}
