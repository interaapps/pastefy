package de.interaapps.pastefy.controller.publicarea;


import de.interaapps.pastefy.exceptions.NotFoundException;
import de.interaapps.pastefy.model.database.User;
import de.interaapps.pastefy.model.responses.user.PublicUserResponse;
import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.httpserver.router.annotation.With;
import org.javawebstack.httpserver.router.annotation.params.Path;
import org.javawebstack.httpserver.router.annotation.verbs.Get;
import org.javawebstack.orm.Repo;

@PathPrefix("/api/v2/public-users")
@With("public-pastes-endpoint")
public class PublicUserController {
    @Get("/{id}")
    public PublicUserResponse getUser(@Path("id") String id) {
        PublicUserResponse publicUserResponse = new PublicUserResponse();

        User user = Repo.get(User.class).where("id", id).first();

        if (user == null)
            throw new NotFoundException();

        publicUserResponse.name = user.uniqueName;
        publicUserResponse.displayName = user.name;
        publicUserResponse.avatar = user.avatar;

        return publicUserResponse;
    }
}
