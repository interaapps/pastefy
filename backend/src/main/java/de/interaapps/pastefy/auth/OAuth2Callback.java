package de.interaapps.pastefy.auth;

import de.interaapps.pastefy.Pastefy;
import de.interaapps.pastefy.model.database.AuthKey;
import de.interaapps.pastefy.model.database.User;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.orm.Repo;
import org.javawebstack.passport.strategies.oauth2.OAuth2Profile;
import org.javawebstack.passport.strategies.oauth2.OAuth2Strategy;

public class OAuth2Callback implements OAuth2Strategy.HttpCallbackHandler {
    public Object handle(Exchange exchange, org.javawebstack.passport.strategies.oauth2.OAuth2Callback callback, String name) {
        User.AuthenticationProvider provider = de.interaapps.pastefy.model.database.User.AuthenticationProvider.getProviderByClass(callback.getProvider().getClass());
        OAuth2Profile profile = callback.getProfile();

        User user = Repo.get(User.class).where("authId", profile.getId()).where("authProvider", provider).first();

        if (user == null) {
            user = new de.interaapps.pastefy.model.database.User();
            int i = 1;
            final String uniqueName = profile.getName().replaceAll("[^a-zA-Z0-9]", "");
            user.uniqueName = uniqueName;
            while (Repo.get(de.interaapps.pastefy.model.database.User.class).where("uniqueName", user.uniqueName).first() != null) {
                user.uniqueName = uniqueName + i++;
            }
            user.authId = profile.getId();
            user.authProvider = provider;

            if (Pastefy.getInstance().getConfig().get("pastefy.grantaccessrequired", "false").equalsIgnoreCase("true"))
                user.type = User.Type.AWAITING_ACCESS;
        }

        // On every login the username, avatar and e-mail gets updated
        user.name = profile.getName();
        user.avatar = profile.getAvatar();
        user.eMail = profile.getMail();
        user.save();

        AuthKey authKey = new AuthKey();
        authKey.refreshToken = callback.getRefreshToken();
        authKey.accessToken = callback.getAccessToken();
        authKey.userId = user.id;
        authKey.type = AuthKey.Type.USER;
        authKey.save();

        exchange.redirect("/auth?key=" + authKey.getKey());
        return "";
    }
}
