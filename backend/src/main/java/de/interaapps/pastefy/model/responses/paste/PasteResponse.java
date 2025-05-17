package de.interaapps.pastefy.model.responses.paste;

import com.google.gson.annotations.SerializedName;
import de.interaapps.pastefy.Pastefy;
import de.interaapps.pastefy.model.database.Paste;
import de.interaapps.pastefy.model.database.User;
import de.interaapps.pastefy.model.elastic.ElasticPaste;
import de.interaapps.pastefy.model.responses.user.PublicUserResponse;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.orm.Repo;

import java.util.List;

public class PasteResponse {
    public boolean exists = false;
    public String id;
    public String content;
    public String title;
    public boolean encrypted = false;
    public String folder;
    public String userId;
    public Paste.Visibility visibility;
    public String forkedFrom;

    @SerializedName("raw_url")
    public String rawURL;
    public Paste.Type type;
    public String createdAt = "0000-00-00 00:00:00";
    public String expireAt = null;
    public List<String> tags;

    public PublicUserResponse user;

    public Boolean starred = null;

    public PasteResponse(ElasticPaste paste) {
        if (paste == null) {
            return;
        }
        id = paste.key;
        rawURL = Pastefy.getInstance().getConfig().get("server.name", "http://localhost") + "/" + id + "/raw";
        title = paste.title;
        content = paste.content;
        createdAt = paste.createdAt.toString();

        if (paste.expireAt != null)
            expireAt = paste.expireAt.toString();

        encrypted = paste.encrypted;
        userId = paste.userId;
        forkedFrom = paste.forkedFrom;
        visibility = paste.visibility;
        if (paste.folder != null)
            folder = paste.folder;
        type = paste.type;
        exists = true;

        //tags = List.of(paste.tags);
        if (paste.user != null) {
            user = new PublicUserResponse(paste.user);
        }
    }
    public PasteResponse(Paste paste, User currentUser, boolean fetchStar, boolean fetchUser) {
        if (paste == null) {
            return;
        }
        id = paste.getKey();
        rawURL = Pastefy.getInstance().getConfig().get("server.name", "http://localhost") + "/" + id + "/raw";
        title = paste.getTitle();
        content = paste.getContent();
        createdAt = paste.createdAt.toString();

        if (paste.expireAt != null)
            expireAt = paste.expireAt.toString();

        encrypted = paste.isEncrypted();
        userId = paste.getUserId();
        forkedFrom = paste.getForkedFrom();
        visibility = paste.getVisibility();
        if (paste.getFolderId() != null)
            folder = paste.getFolderId();
        type = paste.getType();
        exists = true;

        tags = paste.getTags();

        if (fetchUser && paste.getUserId() != null) {
            User pasteUser = paste.getUser();
            if (pasteUser != null) {
                user = new PublicUserResponse(pasteUser);
            }
        }

        if (currentUser != null && fetchStar) {
            starred = currentUser.hasStarred(paste);
        }
    }

    public PasteResponse shortenContent() {
        if (content.length() > 303)
            content = content.substring(0, 300) + "...";
        return this;
    }

    public static PasteResponse create(Paste paste, Exchange exchange, User currentUser, boolean fetchStar, boolean fetchUser) {
        PasteResponse pasteResponse = new PasteResponse(paste, currentUser, fetchStar, fetchUser);
        if (exchange != null) {
            if ("true".equalsIgnoreCase(exchange.query("shorten_content", "false"))) {
                pasteResponse.shortenContent();
            }
        }
        return pasteResponse;
    }
    public static PasteResponse create(Paste paste, Exchange exchange, User currentUser, boolean fetchStar) {
        return create(paste, exchange, currentUser, fetchStar, true);
    }
    public static PasteResponse create(Paste paste, Exchange exchange, User currentUser) {
        return create(paste, exchange, currentUser, true);
    }
}
