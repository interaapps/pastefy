package de.interaapps.pastefy.auth;

import de.interaapps.pastefy.model.auth.User;
import de.interaapps.pastefy.model.database.AuthKey;

public interface AuthenticationProvider {
    User getUser(AuthKey authKey);

    String login(String key);

    User getUserByName(String name);

    boolean isFriend(User user, String name);
}
