package de.interaapps.pastefy.model.responses.user;

import de.interaapps.pastefy.model.database.User;

public class PublicUserResponse {
    public String id;
    public String name;
    public String avatar;
    public String displayName;

    public PublicUserResponse(User user) {
        id = user.id;
        name = user.uniqueName;
        displayName = user.name;
        avatar = user.avatar;
    }
}
