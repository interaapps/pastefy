package de.interaapps.pastefy.controller;

import de.interaapps.accounts.apiclient.AccountsClient;
import de.interaapps.accounts.apiclient.responses.contacts.ContactResponse;
import de.interaapps.pastefy.exceptions.NotFoundException;
import de.interaapps.pastefy.exceptions.PermissionsDeniedException;
import de.interaapps.pastefy.helper.RequestHelper;
import de.interaapps.pastefy.model.database.*;
import de.interaapps.pastefy.model.requests.paste.AddFriendToPasteRequest;
import de.interaapps.pastefy.model.requests.paste.CreatePasteRequest;
import de.interaapps.pastefy.model.requests.paste.EditPasteRequest;
import de.interaapps.pastefy.model.responses.ActionResponse;
import de.interaapps.pastefy.model.responses.paste.CreatePasteResponse;
import de.interaapps.pastefy.model.responses.paste.PasteResponse;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.httpserver.router.annotation.With;
import org.javawebstack.httpserver.router.annotation.params.Attrib;
import org.javawebstack.httpserver.router.annotation.params.Body;
import org.javawebstack.httpserver.router.annotation.params.Path;
import org.javawebstack.httpserver.router.annotation.verbs.Delete;
import org.javawebstack.httpserver.router.annotation.verbs.Get;
import org.javawebstack.httpserver.router.annotation.verbs.Post;
import org.javawebstack.httpserver.router.annotation.verbs.Put;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.query.Query;

import java.util.List;
import java.util.stream.Collectors;

@PathPrefix("/api/v2/paste")
public class PasteController extends HttpController {

    @Post
    @With({"rate-limiter", "auth-login-required-create", "awaiting-access-check", "blocked-check"})
    public CreatePasteResponse create(Exchange exchange, @Body CreatePasteRequest request, @Attrib("user") User user, @Attrib("authkey") AuthKey authKey, @Path("id") String pasteId) {
        if (authKey != null)
            authKey.checkPermission("pastes:create", "pastes:write");

        CreatePasteResponse response = new CreatePasteResponse();

        Paste paste = new Paste();

        Folder folder = Repo.get(Folder.class).where("key", request.folder).first();

        if (user != null) {
            paste.setUserId(user.getId());

            if (folder != null && folder.getUserId().equals(user.getId()))
                paste.setFolder(folder);
        }

        paste.setTitle(request.title);
        paste.setContent(request.content);
        paste.setEncrypted(request.encrypted);
        paste.setType(request.type);

        paste.save();

        response.success = true;
        response.paste = new PasteResponse(paste);

        return response;
    }

    @Get
    public List<PasteResponse> getPastes(Exchange exchange, @Attrib("user") User user, @Attrib("authkey") AuthKey authKey) {
        if (authKey != null)
            authKey.checkPermission("pastes:read");

        Query<Paste> query = Repo.get(Paste.class).query();

        RequestHelper.userIdPastesFilter(user, query, exchange);
        RequestHelper.pagination(query, exchange);
        query.search(exchange.query("search"));
        RequestHelper.queryFilter(query, exchange.getQueryParameters());

        return query.order("created_at", true).all().stream().map(PasteResponse::new).collect(Collectors.toList());
    }

    @Put("/{id}")
    @With({"auth", "awaiting-access-check", "blocked-check"})
    public ActionResponse putPaste(@Body EditPasteRequest request, @Path("id") String id, @Attrib("user") User user, @Attrib("authkey") AuthKey authKey) {
        if (authKey != null)
            authKey.checkPermission("pastes:edit", "pastes:write");

        ActionResponse response = new ActionResponse();
        Paste paste = Repo.get(Paste.class).where("key", id).first();
        if (paste != null) {
            if ((paste.getUserId() != null && paste.getUserId().equals(user.getId())) || user.type == User.Type.ADMIN) {
                if (request.title != null)
                    paste.setTitle(request.title);
                if (request.content != null)
                    paste.setContent(request.content);
                if (request.folder != null)
                    paste.setFolder(request.folder);
                if (request.type != null)
                    paste.setType(request.type);
                if (request.encrypted != null)
                    paste.setEncrypted(request.encrypted);
                paste.save();
                response.success = true;
            } else
                throw new PermissionsDeniedException();
        }
        return response;
    }

    @Get("/{id}")
    @With({"auth-login-required-read", "awaiting-access-check", "blocked-check"})
    public PasteResponse getPaste(Exchange exchange, @Path("id") String id) {
        Paste paste = Repo.get(Paste.class).where("key", id).first();
        if (paste == null)
            throw new NotFoundException();
        return new PasteResponse(paste);
    }

    @Delete("/{id}")
    @With({"auth", "awaiting-access-check", "blocked-check"})
    public ActionResponse deletePaste(Exchange exchange, @Path("id") String id, @Attrib("user") User user, @Attrib("authkey") AuthKey authKey) {
        if (authKey != null)
            authKey.checkPermission("pastes:delete");

        ActionResponse response = new ActionResponse();
        Paste paste = Repo.get(Paste.class).where("key", id).first();

        if (paste != null) {
            if ((paste.getUserId() != null && paste.getUserId().equals(user.getId())) || user.type == User.Type.ADMIN) {
                paste.delete();
                response.success = true;
            } else
                throw new PermissionsDeniedException();
        }

        return response;
    }

    @Post("/{id}/friend")
    @With({"auth", "awaiting-access-check", "blocked-check"})
    public ActionResponse addFriend(Exchange exchange, @Body AddFriendToPasteRequest request, @Path("id") String id, @Attrib("user") User user, @Attrib("authkey") AuthKey requestAuthKey) {
        if (requestAuthKey != null)
            requestAuthKey.checkPermission("pastes.friends:edit");

        ActionResponse response = new ActionResponse();
        Paste paste = Repo.get(Paste.class).where("key", id).first();
        if (paste != null && paste.getUserId().equals(user.getId())) {
            if (user.authProvider == User.AuthenticationProvider.INTERAAPPS) {
                AuthKey authKey = Repo.get(AuthKey.class).where("userId", user.id).where("type", AuthKey.Type.USER).order("createdAt", true).first();

                AccountsClient accountsClient = new AccountsClient(authKey.accessToken);
                for (ContactResponse contact : accountsClient.getContacts().stream().filter(contact -> contact.getName().equalsIgnoreCase(request.friend)).collect(Collectors.toList())) {
                    User friend = Repo.get(User.class).where("name", contact.getName()).where("authProvider", User.AuthenticationProvider.INTERAAPPS).first();
                    SharedPaste sharedPaste = new SharedPaste();
                    sharedPaste.setUser(user);
                    sharedPaste.setTarget(friend);
                    sharedPaste.setPaste(paste);
                    sharedPaste.save();

                    Notification notification = new Notification();
                    notification.setMessage(user.getName() + " shared a paste with you! Click to open.");
                    notification.url = "/" + paste.getKey();
                    friend.sendNotification(notification);
                    response.success = true;
                }
            } else {
                throw new RuntimeException("NOT IMPLEMENTED");
            }

        }
        return response;
    }


}
