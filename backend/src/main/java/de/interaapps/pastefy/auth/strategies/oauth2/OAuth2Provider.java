package de.interaapps.pastefy.auth.strategies.oauth2;

import org.javawebstack.abstractdata.AbstractObject;

public interface OAuth2Provider {
    OAuth2Callback callback(AbstractObject queryParameters, String callbackUrl);
    String redirect(String callbackUrl);
}
