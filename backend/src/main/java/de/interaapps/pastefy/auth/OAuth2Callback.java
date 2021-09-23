package de.interaapps.pastefy.auth;

import de.interaapps.pastefy.model.database.AuthKey;
import de.interaapps.pastefy.model.database.User;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.orm.Repo;
import org.javawebstack.passport.Profile;
import org.javawebstack.passport.services.oauth2.*;

import java.util.HashMap;
import java.util.Map;

public class OAuth2Callback  implements OAuth2CallbackHandler {
    private static Map<String, Class<? extends OAuth2Service>> oauthServicesClasses = new HashMap<>();
    static {
        oauthServicesClasses.put("interaapps", InteraAppsOAuth2Service.class);
        oauthServicesClasses.put("github", GithubOAuth2Service.class);
        oauthServicesClasses.put("google", GoogleOAuth2Service.class);
        oauthServicesClasses.put("twitch", TwitchOAuth2Service.class);
        oauthServicesClasses.put("discord", DiscordOAuth2Service.class);
    }

    @Override
    public Object callback(String s, Exchange exchange, org.javawebstack.passport.services.oauth2.OAuth2Callback oAuth2Callback) {
        User.AuthenticationProvider provider = de.interaapps.pastefy.model.database.User.AuthenticationProvider.getProviderByClass(oauthServicesClasses.get(s));
        Profile profile = oAuth2Callback.getProfile();

        User user = Repo.get(User.class).where("authId", profile.getId()).where("authProvider", provider).first();

        if (user == null) {
            user = new de.interaapps.pastefy.model.database.User();
            int i = 1;
            final String uniqueName = profile.getName().replaceAll("[^a-zA-Z0-9]", "");
            user.uniqueName = uniqueName;
            while (Repo.get(de.interaapps.pastefy.model.database.User.class).where("uniqueName", user.uniqueName).first() != null) {
                user.uniqueName = uniqueName+i++;
            }
            user.authId = profile.getId();
            user.authProvider = provider;
        }
        // On every login the username, avatar and e-mail gets updated
        user.name = profile.getName();
        user.avatar = profile.getAvatar();
        user.eMail = profile.getMail();
        user.save();

        AuthKey authKey = new AuthKey();
        authKey.refreshToken = oAuth2Callback.getRefreshToken();
        authKey.accessToken = oAuth2Callback.getToken();
        authKey.userId = user.id;
        authKey.type = AuthKey.Type.USER;
        authKey.save();

        exchange.redirect("/auth?key=" + authKey.getKey());
        return "";
    }
}
