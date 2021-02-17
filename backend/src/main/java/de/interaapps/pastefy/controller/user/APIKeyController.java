package de.interaapps.pastefy.controller.user;

import de.interaapps.pastefy.model.auth.User;
import de.interaapps.pastefy.model.database.AuthKey;
import de.interaapps.pastefy.model.responses.ActionResponse;
import de.interaapps.pastefy.model.responses.user.keys.CreateAuthKeyResponse;
import org.javawebstack.framework.HttpController;
import org.javawebstack.httpserver.router.annotation.*;
import org.javawebstack.orm.Repo;

import java.util.List;
import java.util.stream.Collectors;

@PathPrefix("/api/v2/user/keys")
public class APIKeyController extends HttpController {
    @Post
    @With("auth")
    public CreateAuthKeyResponse addKey(@Attrib("user") User user) {
        CreateAuthKeyResponse response = new CreateAuthKeyResponse();
        AuthKey authKey = new AuthKey();
        AuthKey userAuthKey = Repo.get(AuthKey.class).where("userId", user.getId()).order("createdAt", true).first();
        if (userAuthKey != null) {
            authKey.type = AuthKey.Type.API;
            authKey.userId = userAuthKey.userId;
            authKey.apiKey = userAuthKey.apiKey;
            authKey.save();

            response.success = true;
            response.key = authKey.getKey();
        }
        return response;
    }

    @Get
    @With("auth")
    public List<String> getKeys(@Attrib("user") User user) {
        return Repo.get(AuthKey.class).where("type", AuthKey.Type.API).where("userId", user.getId()).all().stream().map(authKey -> authKey.getKey()).collect(Collectors.toList());
    }

    @Delete("/{key}")
    @With("auth")
    public ActionResponse delete(@Attrib("user") User user, @Path("key") String key) {
        Repo.get(AuthKey.class).where("key", key).where("userId", user.getId()).all().forEach(authKey -> authKey.delete());
        return new ActionResponse(true);
    }

}
