package de.interaapps.pastefy.model.responses.user;

import de.interaapps.pastefy.model.database.User;
import de.interaapps.pastefy.model.elastic.ElasticUser;

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

    public PublicUserResponse(ElasticUser user) {
        id = user.id;
        name = user.uniqueName;
        displayName = user.name;
        avatar = user.avatar;
    }
}
