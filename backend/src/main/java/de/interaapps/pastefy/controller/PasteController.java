package de.interaapps.pastefy.controller;

import de.interaapps.pastefy.auth.AuthenticationProvider;
import de.interaapps.pastefy.model.auth.User;
import de.interaapps.pastefy.model.database.Folder;
import de.interaapps.pastefy.model.database.Notification;
import de.interaapps.pastefy.model.database.Paste;
import de.interaapps.pastefy.model.database.SharedPaste;
import de.interaapps.pastefy.model.requests.paste.AddFriendToPasteRequest;
import de.interaapps.pastefy.model.requests.paste.CreatePasteRequest;
import de.interaapps.pastefy.model.responses.ActionResponse;
import de.interaapps.pastefy.model.responses.paste.CreatePasteResponse;
import de.interaapps.pastefy.model.responses.paste.PasteResponse;
import org.javawebstack.framework.HttpController;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.router.annotation.*;
import org.javawebstack.injector.Inject;
import org.javawebstack.orm.Repo;

@PathPrefix("/api/v2/paste")
public class PasteController extends HttpController {

    @Inject
    private AuthenticationProvider authenticationProvider;

    @Post
    public CreatePasteResponse create(Exchange exchange, @Body CreatePasteRequest request, @Attrib("user") User user){
        CreatePasteResponse response = new CreatePasteResponse();

        Paste paste = new Paste();
        paste.setTitle(request.title);
        paste.setContent(request.content);
        paste.setEncrypted(request.encrypted);

        Folder folder = Repo.get(Folder.class).where("key", request.folder).get();

        if (user != null) {
            paste.setUserId(user.getId());

            if (folder != null && folder.getUserId() == user.getId())
                paste.setFolder(folder);
        }
        paste.save();

        response.success = true;
        response.paste   = new PasteResponse(paste);

        return response;
    }

    @Get("/{id}")
    public PasteResponse getPaste(Exchange exchange, @Path("id") String id){
        Paste paste = Repo.get(Paste.class).where("key", id).get();
        return new PasteResponse(paste);
    }

    @Delete("/{id}")
    @With("auth")
    public ActionResponse getPaste(Exchange exchange, @Path("id") String id, @Attrib("user") User user){
        ActionResponse response = new ActionResponse();
        Paste paste = Repo.get(Paste.class).where("key", id).get();

        if (paste != null){
            if (paste.getUserId() == user.getId()) {
                paste.delete();
                response.success = true;
            }
        }

        return response;
    }

    @Post("/{id}/friend")
    @With("auth")
    public ActionResponse addFriend(Exchange exchange, @Body AddFriendToPasteRequest request, @Path("id") String id, @Attrib("user") User user){
        ActionResponse response = new ActionResponse();
        Paste paste = Repo.get(Paste.class).where("key", id).get();
        if (paste != null && paste.getUserId() == user.getId()) {
            if (authenticationProvider.isFriend(user, request.friend)) {
                User friend = authenticationProvider.getUserByName(request.friend);
                SharedPaste sharedPaste = new SharedPaste();
                sharedPaste.setUser(user);
                sharedPaste.setTarget(friend);
                sharedPaste.setPaste(paste);
                sharedPaste.save();

                Notification notification = new Notification();
                notification.setMessage(user.getName() + " shared a paste with you! Click to open.");
                notification.url = "/"+paste.getKey();
                friend.sendNotification(notification);
                response.success = true;
            }
        }
        return response;
    }


}
