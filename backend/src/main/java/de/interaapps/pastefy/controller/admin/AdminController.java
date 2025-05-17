package de.interaapps.pastefy.controller.admin;

import de.interaapps.pastefy.controller.HttpController;
import de.interaapps.pastefy.helper.RequestHelper;
import de.interaapps.pastefy.model.database.*;
import de.interaapps.pastefy.model.requests.admin.EditUserRequest;
import de.interaapps.pastefy.model.responses.ActionResponse;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.router.annotation.PathPrefix;
import org.javawebstack.http.router.router.annotation.With;
import org.javawebstack.http.router.router.annotation.params.Attrib;
import org.javawebstack.http.router.router.annotation.params.Body;
import org.javawebstack.http.router.router.annotation.params.Path;
import org.javawebstack.http.router.router.annotation.verbs.Delete;
import org.javawebstack.http.router.router.annotation.verbs.Get;
import org.javawebstack.http.router.router.annotation.verbs.Put;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.query.Query;

import java.util.List;

@PathPrefix("/api/v2/admin")
@With("admin")
public class AdminController extends HttpController {
    @Get("/users")
    public List<User> getUsers(Exchange exchange, @Attrib("user") User user, @Attrib("authkey") AuthKey authKey) {
        authKey.checkPermission("admin.users:read");
        Query<User> query = Repo.get(User.class).query();

        RequestHelper.pagination(query, exchange);
        query.search(exchange.query("search"));
        RequestHelper.queryFilter(query, exchange.getQueryParameters());

        return query.order("created_at", true).all();
    }

    @Get("/users/{id}")
    public User getUser(@Path("id") String id, @Attrib("authkey") AuthKey authKey) {
        authKey.checkPermission("admin.users:read");
        return Repo.get(User.class).get(id);
    }

    @Delete("/users/{id}")
    public ActionResponse removeUser(@Path("id") String id, @Attrib("authkey") AuthKey authKey) {
        authKey.checkPermission("admin.users:delete");

        Repo.get(User.class).get(id).delete();

        return new ActionResponse(true);
    }

    @Put("/users/{id}")
    public ActionResponse editUser(@Body EditUserRequest request, @Attrib("authkey") AuthKey authKey, @Path("id") String id) {
        authKey.checkPermission("admin.users:edit");

        User user = Repo.get(User.class).get(id);

        if (request.uniqueName != null)
            user.uniqueName = request.uniqueName;

        if (request.name != null)
            user.name = request.name;

        if (request.type != null)
            user.type = request.type;

        user.save();

        return new ActionResponse(true);
    }
}
