package de.interaapps.pastefy.auth.strategies.oauth2;

import org.javawebstack.httpclient.HTTPClient;

public abstract class OAuth2Callback {
    protected String accessToken;
    protected String refreshToken;
    protected OAuth2Provider provider;

    public OAuth2Callback(OAuth2Provider provider, String accessToken, String refreshToken) {
        this.provider = provider;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public abstract HTTPClient createApiClient();

    public abstract OAuth2Profile getProfile();

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public OAuth2Provider getProvider() {
        return provider;
    }
}
