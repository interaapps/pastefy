package de.interaapps.pastefy.controller.user;

import de.interaapps.pastefy.controller.HttpController;
import de.interaapps.pastefy.exceptions.PermissionsDeniedException;
import de.interaapps.pastefy.model.database.*;
import de.interaapps.pastefy.model.responses.folder.FolderResponse;
import de.interaapps.pastefy.model.responses.paste.PasteResponse;
import de.interaapps.pastefy.model.responses.user.UserPastesResponse;
import de.interaapps.pastefy.model.responses.user.UserResponse;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.httpserver.router.annotation.With;
import org.javawebstack.httpserver.router.annotation.params.Attrib;
import org.javawebstack.httpserver.router.annotation.verbs.Get;
import org.javawebstack.orm.Repo;

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
    @With("auth")
    public UserPastesResponse userPastes(Exchange exchange, @Attrib("user") User user, @Attrib("authkey") AuthKey authKey) {
        if (authKey != null && (!authKey.hasPermission("pastes:read") || !authKey.hasPermission("folders:read")))
            throw new PermissionsDeniedException();

        UserPastesResponse response = new UserPastesResponse();
        int page = 0;
        if (exchange.rawRequest().getParameter("page") != null)
            page = Integer.parseInt(exchange.rawRequest().getParameter("page")) - 1;

        response.pastes = Repo.get(Paste.class).where("userId", user.getId()).isNull("folder").order("updated_at", true).limit(10).offset(page * 10).all().stream().map(paste -> new PasteResponse(paste).shortenContent()).collect(Collectors.toList());
        response.folder = Repo.get(Folder.class).where("userId", user.getId()).isNull("parent").order("updated_at", true).all().stream().map(folder -> new FolderResponse(folder, exchange.rawRequest().getParameter("hide_children") == null)).collect(Collectors.toList());

        return response;
    }

    @Get("/folders")
    @With("auth")
    public List<FolderResponse> getFolder(Exchange exchange, @Attrib("user") User user, @Attrib("authkey") AuthKey authKey) {
        if (authKey != null)
            authKey.checkPermission("folders:read");

        return user.getFolderTree(exchange.rawRequest().getParameter("hide_children") == null, exchange.rawRequest().getParameter("hide_sub_children") == null, exchange.rawRequest().getParameter("hide_pastes") == null);
    }

    @Get("/pastes")
    @With("auth")
    public List<PasteResponse> getPastes(@Attrib("user") User user, @Attrib("authkey") AuthKey authKey) {
        if (authKey != null)
            authKey.checkPermission("pastes:read");

        return user.getPastes().stream().map(PasteResponse::new).collect(Collectors.toList());
    }

    @Get("/sharedpastes")
    @With("auth")
    public List<PasteResponse> getSharedPastes(Exchange exchange, @Attrib("user") User user, @Attrib("authkey") AuthKey authKey) {
        if (authKey != null)
            authKey.checkPermission("sharedpastes:read");

        List<PasteResponse> pastes = new ArrayList<>();
        int page = 0;
        if (exchange.rawRequest().getParameter("page") != null)
            page = Integer.parseInt(exchange.rawRequest().getParameter("page")) - 1;

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
                    return new PasteResponse(paste);
                })
                .collect(Collectors.toList());

        return pastes;
    }


}
