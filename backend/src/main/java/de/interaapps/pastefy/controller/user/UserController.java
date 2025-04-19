package de.interaapps.pastefy.controller.user;

import de.interaapps.pastefy.controller.HttpController;
import de.interaapps.pastefy.exceptions.PermissionsDeniedException;
import de.interaapps.pastefy.helper.RequestHelper;
import de.interaapps.pastefy.model.database.*;
import de.interaapps.pastefy.model.responses.folder.FolderResponse;
import de.interaapps.pastefy.model.responses.paste.PasteResponse;
import de.interaapps.pastefy.model.responses.user.UserPastesResponse;
import de.interaapps.pastefy.model.responses.user.UserResponse;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.router.annotation.PathPrefix;
import org.javawebstack.http.router.router.annotation.With;
import org.javawebstack.http.router.router.annotation.params.Attrib;
import org.javawebstack.http.router.router.annotation.verbs.Get;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@PathPrefix("/api/v2/user")
public class UserController extends HttpController {

    @Get
    public UserResponse getUser(@Attrib("user") User user) {
        return new UserResponse(user);
    }

    @Get("/overview")
    @With({"auth", "awaiting-access-check", "blocked-check"})
    public UserPastesResponse userPastes(Exchange exchange, @Attrib("user") User user, @Attrib("authkey") AuthKey authKey) {
        if (authKey != null && (!authKey.hasPermission("pastes:read") || !authKey.hasPermission("folders:read")))
            throw new PermissionsDeniedException();

        UserPastesResponse response = new UserPastesResponse();
        int page = 0;
        if (exchange.getQueryParameters().has("page"))
            page = Integer.parseInt(exchange.getQueryParameters().get("page").string()) - 1;

        response.pastes = Repo.get(Paste.class).where("userId", user.getId()).whereNull("folder").order("updated_at", true).limit(10).offset(page * 10).all().stream().map(paste -> PasteResponse.create(paste, exchange, user, false, false)).collect(Collectors.toList());
        response.folder = Repo.get(Folder.class).where("userId", user.getId()).whereNull("parent").order("updated_at", true).all().stream().map(folder -> new FolderResponse(folder, !exchange.getQueryParameters().has("hide_children"))).collect(Collectors.toList());

        return response;
    }

    @Get("/folders")
    @With({"auth", "awaiting-access-check", "blocked-check"})
    public List<FolderResponse> getFolder(Exchange exchange, @Attrib("user") User user, @Attrib("authkey") AuthKey authKey) {
        if (authKey != null)
            authKey.checkPermission("folders:read");

        return user.getFolderTree(
                !exchange.getQueryParameters().has("hide_children"),
                !exchange.getQueryParameters().has("hide_sub_children"),
                !exchange.getQueryParameters().has("hide_pastes")
        );
    }

    @Get("/pastes")
    @With({"auth", "awaiting-access-check", "blocked-check"})
    public List<PasteResponse> getPastes(Exchange exchange, @Attrib("user") User user, @Attrib("authkey") AuthKey authKey) {
        if (authKey != null)
            authKey.checkPermission("pastes:read");

        Query<Paste> query = Repo.get(Paste.class).query().where("userId", user.id).order("createdAt", true);

        if ("true".equalsIgnoreCase(exchange.query("hide_children", "false"))) {
            query.whereNull("folder");
        }

        RequestHelper.pagination(query, exchange);
        query.search(exchange.query("search"));
        RequestHelper.queryFilter(query, exchange.getQueryParameters());
        RequestHelper.filterTags(query, exchange.getQueryParameters());


        return query.all().stream().map(p -> PasteResponse.create(p, exchange, user, false, false)).collect(Collectors.toList());
    }

    @Get("/sharedpastes")
    @With({"auth", "awaiting-access-check", "blocked-check"})
    public List<PasteResponse> getSharedPastes(Exchange exchange, @Attrib("user") User user, @Attrib("authkey") AuthKey authKey) {
        if (authKey != null)
            authKey.checkPermission("sharedpastes:read");

        List<PasteResponse> pastes = new ArrayList<>();
        int page = 0;
        if (exchange.getQueryParameters().has("page"))
            page = Integer.parseInt(exchange.getQueryParameters().get("page").string()) - 1;

        pastes = Repo.get(SharedPaste.class)
                .where("targetId", user.getId())
                .limit(10)
                .offset(page * 10)
                .order("updated_at", true)
                .all().stream()
                .map(sharedPaste -> {
                    Paste paste = sharedPaste.getPaste();
                    if (paste == null)
                        sharedPaste.delete();
                    return PasteResponse.create(paste, exchange, user, false, false);
                })
                .collect(Collectors.toList());

        return pastes;
    }

    @Get("/starred-pastes")
    @With({"auth", "awaiting-access-check", "blocked-check"})
    public List<PasteResponse> getStarredPastes(Exchange exchange, @Attrib("user") User user, @Attrib("authkey") AuthKey authKey) {
        if (authKey != null)
            authKey.checkPermission("stars:read");

        Query<Paste> query = Repo.get(Paste.class)
                .query()
                .whereExists(PasteStar.class, (q) -> q
                        .where(PasteStar.class, "paste", "=", Paste.class, "key")
                        .where("userId", user.id)
                );

        RequestHelper.pagination(query, exchange);
        query.search(exchange.query("search"));
        RequestHelper.queryFilter(query, exchange.getQueryParameters());
        RequestHelper.filterTags(query, exchange.getQueryParameters());

        return query.order("created_at", true).all().stream().map(p -> PasteResponse.create(p, exchange, user, false, true)).collect(Collectors.toList());
    }


}
