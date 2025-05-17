package de.interaapps.pastefy.model.elastic;

import de.interaapps.pastefy.model.database.User;

import java.sql.Timestamp;

public class ElasticUser {
    public String id;
    public String name;
    public String uniqueName;
    public String eMail;
    public String avatar;
    public String authId;
    public User.AuthenticationProvider authProvider;
    public User.Type type = User.Type.USER;
    public Timestamp createdAt;
    public Timestamp updatedAt;


    public static ElasticUser fromUser(User user) {
        ElasticUser elasticUser = new ElasticUser();
        elasticUser.id = user.id;
        elasticUser.name = user.name;
        elasticUser.uniqueName = user.uniqueName;
        elasticUser.eMail = user.eMail;
        elasticUser.avatar = user.avatar;
        elasticUser.authId = user.authId;
        elasticUser.authProvider = user.authProvider;
        elasticUser.type = user.type;
        elasticUser.createdAt = user.createdAt;
        elasticUser.updatedAt = user.updatedAt;

        return elasticUser;
    }
}
