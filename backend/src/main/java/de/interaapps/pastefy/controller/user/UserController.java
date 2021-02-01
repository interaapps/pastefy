package de.interaapps.pastefy.controller.user;

import de.interaapps.pastefy.auth.AuthenticationProvider;
import de.interaapps.pastefy.model.auth.User;
import de.interaapps.pastefy.model.database.Folder;
import de.interaapps.pastefy.model.database.Paste;
import de.interaapps.pastefy.model.database.SharedPaste;
import de.interaapps.pastefy.model.responses.folder.FolderResponse;
import de.interaapps.pastefy.model.responses.paste.PasteResponse;
import de.interaapps.pastefy.model.responses.user.UserPastesResponse;
import de.interaapps.pastefy.model.responses.user.UserResponse;
import org.javawebstack.framework.HttpController;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.router.annotation.Attrib;
import org.javawebstack.httpserver.router.annotation.Get;
import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.httpserver.router.annotation.With;
import org.javawebstack.injector.Inject;
import org.javawebstack.orm.Repo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@PathPrefix("/api/v2/user")
public class UserController extends HttpController {
    @Inject
    public AuthenticationProvider authenticationProvider;

    @Get
    public UserResponse getUser(@Attrib("user") User user){
        return new UserResponse(user);
    }

    @Get("/overview")
    @With("auth")
    public UserPastesResponse userPastes(Exchange exchange, @Attrib("user") User user){
        UserPastesResponse response = new UserPastesResponse();
        int page = 0;
        if (exchange.rawRequest().getParameter("page") != null)
            page = Integer.parseInt(exchange.rawRequest().getParameter("page"))-1;

        response.pastes = Repo.get(Paste.class).where("userId", user.getId()).isNull("folder").order("updated_at", true).limit(13).offset(page*13).all().stream().map(PasteResponse::new).collect(Collectors.toList());
        response.folder = Repo.get(Folder.class).where("userId", user.getId()).isNull("parent").order("updated_at", true).all().stream().map(FolderResponse::new).collect(Collectors.toList());

        return response;
    }

    @Get("/folders")
    @With("auth")
    public List<FolderResponse> getFolder(@Attrib("user") User user){
        List<FolderResponse> folders = new ArrayList<>();
        folders = user.getFolders().stream().map(FolderResponse::new).collect(Collectors.toList());
        return folders;
    }

    @Get("/pastes")
    @With("auth")
    public List<PasteResponse> getPastes(@Attrib("user") User user){
        List<PasteResponse> pastes = new ArrayList<>();
        pastes = user.getPastes().stream().map(PasteResponse::new).collect(Collectors.toList());
        return pastes;
    }

    @Get("/sharedpastes")
    @With("auth")
    public List<PasteResponse> getSharedPastes(Exchange exchange, @Attrib("user") User user){
        List<PasteResponse> pastes = new ArrayList<>();
        int page = 0;
        if (exchange.rawRequest().getParameter("page") != null)
            page = Integer.parseInt(exchange.rawRequest().getParameter("page"))-1;

        pastes = Repo.get(SharedPaste.class)
                .where("targetId", user.getId())
                .limit(10)
                .offset(page*10)
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
