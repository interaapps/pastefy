package de.interaapps.pastefy.auth.strategies.oauth2.providers;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;
import de.interaapps.pastefy.auth.strategies.oauth2.OAuth2Profile;
import de.interaapps.pastefy.auth.strategies.oauth2.OAuth2Provider;
import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.httpclient.HTTPClient;

import java.io.IOException;
import java.util.Arrays;

public class GoogleOAuth2Provider implements OAuth2Provider {
    private String clientId;
    private String secret;
    private GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow;

    public GoogleOAuth2Provider(String clientId, String secret) {
        this.clientId = clientId;
        this.secret = secret;

        googleAuthorizationCodeFlow = new GoogleAuthorizationCodeFlow.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                clientId,
                secret,
                Arrays.asList(
                        "https://www.googleapis.com/auth/userinfo.profile",
                        "https://www.googleapis.com/auth/userinfo.email"
                )
        ).build();
    }

    public GoogleOAuth2Provider setScopes(String[] scopes) {
        googleAuthorizationCodeFlow.getScopes().clear();
        for (String scope : scopes) {
            googleAuthorizationCodeFlow.getScopes().add(scope);
        }
        return this;
    }

    public OAuth2Callback callback(AbstractObject queryParameters, String callbackUrl) {
        try {
            GoogleTokenResponse code = googleAuthorizationCodeFlow.newTokenRequest(queryParameters.string("code"))
                    .setRedirectUri(callbackUrl)
                    .execute();
            return new OAuth2Callback(this, code.getAccessToken(), code.getRefreshToken(), clientId, secret);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String redirect(String callbackUrl) {
        return googleAuthorizationCodeFlow
                .newAuthorizationUrl()
                .setAccessType("offline")
                .setRedirectUri(callbackUrl)
                .build();
    }

    public static class OAuth2Callback extends de.interaapps.pastefy.auth.strategies.oauth2.OAuth2Callback {
        private String clientId;
        private String secret;

        public OAuth2Callback(OAuth2Provider provider, String accessToken, String refreshToken, String clientId, String secret) {
            super(provider, accessToken, refreshToken);
            this.clientId = clientId;
            this.secret = secret;
        }

        public HTTPClient createApiClient() {
            return new HTTPClient("https://www.googleapis.com/oauth2/v1").bearer(accessToken);
        }

        public OAuth2Profile getProfile() {
            Credential credential = new GoogleCredential.Builder()
                    .setTransport(new NetHttpTransport())
                    .setJsonFactory(GsonFactory.getDefaultInstance())
                    .setClientSecrets(clientId, secret)
                    .build().setAccessToken(accessToken);

            Oauth2 oauth2 = new Oauth2.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential
            ).setApplicationName(clientId).build();
            try {
                OAuth2Profile profile = new OAuth2Profile();
                Userinfo userinfo = oauth2.userinfo().get().execute();
                profile.id = userinfo.getId();
                profile.name = userinfo.getName();
                profile.avatar = userinfo.getPicture();
                profile.mail = userinfo.getEmail();
                userinfo.forEach((key, val) -> {
                    profile.set(key, val.toString());
                });
                return profile;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
