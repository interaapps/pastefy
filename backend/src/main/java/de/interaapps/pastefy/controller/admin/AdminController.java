package de.interaapps.pastefy.controller.admin;

import de.interaapps.pastefy.controller.HttpController;
import de.interaapps.pastefy.helper.RequestHelper;
import de.interaapps.pastefy.model.database.AuthKey;
import de.interaapps.pastefy.model.database.Folder;
import de.interaapps.pastefy.model.database.User;
import de.interaapps.pastefy.model.requests.admin.EditUserRequest;
import de.interaapps.pastefy.model.responses.ActionResponse;
import de.interaapps.pastefy.model.responses.folder.FolderResponse;
import de.interaapps.pastefy.model.responses.user.UserResponse;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.httpserver.router.annotation.With;
import org.javawebstack.httpserver.router.annotation.params.Attrib;
import org.javawebstack.httpserver.router.annotation.params.Body;
import org.javawebstack.httpserver.router.annotation.params.Path;
import org.javawebstack.httpserver.router.annotation.verbs.Delete;
import org.javawebstack.httpserver.router.annotation.verbs.Get;
import org.javawebstack.httpserver.router.annotation.verbs.Put;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.query.Query;

import java.util.List;

@PathPrefix("/api/v2/admin")
public class AdminController extends HttpController {
    @Get("/users")
    @With("admin")
    public List<User> getUsers(Exchange exchange, @Attrib("user") User user, @Attrib("authkey") AuthKey authKey){
        authKey.checkPermission("admin.users:read");
        Query<User> query = Repo.get(User.class).query();

        RequestHelper.userIdPastesFilter(user, query, exchange);
        query.search(exchange.query("search"));
        RequestHelper.queryFilter(query, exchange.getQueryParameters());

        return query.all();
    }

    @Get("/users/{id}")
    @With("admin")
    public User getUser(@Path("id") String id, @Attrib("authkey") AuthKey authKey){
        authKey.checkPermission("admin.users:read");
        return Repo.get(User.class).get(id);
    }

    @Delete("/users/{id}")
    @With("admin")
    public ActionResponse removeUser(@Path("id") String id, @Attrib("authkey") AuthKey authKey){
        authKey.checkPermission("admin.users:delete");
        Repo.get(User.class).get(id).delete();
        return new ActionResponse(true);
    }

    @Put("/users/{id}")
    @With("admin")
    public ActionResponse editUser(@Body EditUserRequest request, @Attrib("authkey") AuthKey authKey, @Path("id") String id){
        authKey.checkPermission("admin.users:edit");

        User user = Repo.get(User.class).get(id);

        if (request.uniqueName != null)
            user.uniqueName = request.uniqueName;

        if (request.name != null)
            user.name = request.name;

        if (request.type != null)
            user.type = request.type;

        return new ActionResponse(true);
    }
}
