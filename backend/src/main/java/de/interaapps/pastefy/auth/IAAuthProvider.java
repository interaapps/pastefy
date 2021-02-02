package de.interaapps.pastefy.auth;

import de.interaapps.pastefy.model.auth.IAAuthUser;
import de.interaapps.pastefy.model.auth.User;
import de.interaapps.pastefy.model.database.AuthKey;
import org.javawebstack.abstractdata.AbstractArray;
import org.javawebstack.abstractdata.AbstractElement;
import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.httpclient.HTTPClient;

public class IAAuthProvider implements AuthenticationProvider {

    private final HTTPClient iaAuthClient;
    private String apiKey;

    public IAAuthProvider(String apiKey){
        this.apiKey = apiKey;
        iaAuthClient =  new HTTPClient()
                .setBaseUrl("https://accounts.interaapps.de/iaauth/api");
    }

    public User getUser(AuthKey authKey) {
        IAAuthUser authUser =  iaAuthClient.post("/getuserinformation")
                .body(new AbstractObject()
                        .set("userkey", authKey != null ? authKey.apiKey : "")
                        .set("key", apiKey)
                        .toFormData().toString())
                .object(IAAuthUser.class);
        return authUser.valid ? authUser : null;
    }

    @Override
    public String login(String key) {

        AuthKey authKey = new AuthKey();
        authKey.apiKey = key;
        authKey.type = AuthKey.Type.USER;
        IAAuthUser iaAuthUser = (IAAuthUser) getUser(authKey);
        authKey.userId = iaAuthUser.id;
        if (iaAuthUser.valid) {
            authKey.save();
            authKey.userId = iaAuthUser.id;
            return authKey.getKey();
        }

        return null;
    }

    public User findUser(String key, String operator, String value){
        AbstractElement abstractElement = iaAuthClient.post("/finduser")
                .body(new AbstractObject()
                        .set("key", apiKey)
                        .set("query", new AbstractArray().add(new AbstractArray().add(key).add(operator).add(value)).toJsonString())
                        .toFormData().toString())
                .data();

        IAAuthUser user = iaAuthClient.post("/getuserinformation")
                .body(new AbstractObject()
                        .set("userkey", abstractElement.object().get("userkey"))
                        .set("key", apiKey)
                        .toFormData().toString())
                .object(IAAuthUser.class);

        return user.valid ? user : null;
    }

    public User getUserByName(String name) {
        return findUser("name", "=", name);
    }

    public boolean isFriend(User user, String name) {
         return iaAuthClient.post("/friends/isfriend")
                .body(new AbstractObject()
                        .set("userkey", ((IAAuthUser) user).userKey)
                        .set("key", apiKey)
                        .set("name", name)
                        .toFormData().toString())
                .data().object().get("is_friend").bool();
    }
}
