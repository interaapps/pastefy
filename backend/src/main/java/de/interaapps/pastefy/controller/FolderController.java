package de.interaapps.pastefy.controller;

import de.interaapps.pastefy.exceptions.NotFoundException;
import de.interaapps.pastefy.helper.RequestHelper;
import de.interaapps.pastefy.model.database.AuthKey;
import de.interaapps.pastefy.model.database.Folder;
import de.interaapps.pastefy.model.database.User;
import de.interaapps.pastefy.model.requests.CreateFolderRequest;
import de.interaapps.pastefy.model.responses.ActionResponse;
import de.interaapps.pastefy.model.responses.folder.CreateFolderResponse;
import de.interaapps.pastefy.model.responses.folder.FolderResponse;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.httpserver.router.annotation.With;
import org.javawebstack.httpserver.router.annotation.params.Attrib;
import org.javawebstack.httpserver.router.annotation.params.Body;
import org.javawebstack.httpserver.router.annotation.params.Path;
import org.javawebstack.httpserver.router.annotation.verbs.Delete;
import org.javawebstack.httpserver.router.annotation.verbs.Get;
import org.javawebstack.httpserver.router.annotation.verbs.Post;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.query.Query;

import java.util.List;
import java.util.stream.Collectors;

@PathPrefix("/api/v2/folder")
public class FolderController extends HttpController {
    @Post
    @With({"auth", "awaiting-access-check", "blocked-check"})
    public CreateFolderResponse createFolder(Exchange exchange, @Body CreateFolderRequest request, @Attrib("user") User user, @Attrib("authkey") AuthKey authKey) {
        if (authKey != null)
            authKey.checkPermission("folders:create");

        CreateFolderResponse response = new CreateFolderResponse();

        Folder folder = new Folder();
        folder.setUserId(user.getId());
        folder.setName(request.name);

        Folder parent = Repo.get(Folder.class).where("key", request.parent).first();

        if (parent != null && parent.getUserId().equals(user.getId()))
            folder.setParent(parent);

        folder.save();
        response.success = true;
        response.folder = new FolderResponse(folder);

        return response;
    }

    @Get
    public List<FolderResponse> getFolder(Exchange exchange, @Attrib("user") User user, @Attrib("authkey") AuthKey authKey) {
        if (authKey != null)
            authKey.checkPermission("folders:read");

        Query<Folder> query = Repo.get(Folder.class).query();

        RequestHelper.userIdPastesFilter(user, query, exchange);
        RequestHelper.pagination(query, exchange);
        query.search(exchange.query("search"));
        RequestHelper.queryFilter(query, exchange.getQueryParameters());

        return query.order("created_at", true).all().stream().map(f -> new FolderResponse(f, false, false, false)).collect(Collectors.toList());
    }

    @Get("/{id}")
    @With({"auth-login-required-read", "awaiting-access-check", "blocked-check"})
    public FolderResponse getFolder(Exchange exchange, @Path("id") String id, @Attrib("user") User user, @Attrib("authkey") AuthKey authKey) {
        if (authKey != null)
            authKey.checkPermission("folders:read");

        Folder folder = Repo.get(Folder.class).where("key", id).first();
        if (folder == null)
            throw new NotFoundException();
        return new FolderResponse(folder, true, exchange.rawRequest().getParameter("hide_children") == null);
    }

    @Delete("/{id}")
    @With({"auth", "awaiting-access-check", "blocked-check"})
    public ActionResponse delete(Exchange exchange, @Path("id") String id, @Attrib("user") User user, @Attrib("authkey") AuthKey authKey) {
        if (authKey != null)
            authKey.checkPermission("folders:delete");

        ActionResponse response = new ActionResponse();
        Folder folder = Repo.get(Folder.class).where("key", id).first();

        if (folder != null) {
            if (folder.getUserId().equals(user.getId()) || user.type == User.Type.ADMIN) {
                folder.delete();
                response.success = true;
            }
        }

        return response;
    }
}
