package de.interaapps.pastefy.model.responses.paste;

import com.google.gson.annotations.SerializedName;
import de.interaapps.pastefy.Pastefy;
import de.interaapps.pastefy.model.database.Paste;
import org.javawebstack.httpserver.Exchange;

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

    public PasteResponse(Paste paste) {
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
    }

    public PasteResponse shortenContent() {
        if (content.length() > 303)
            content = content.substring(0, 300) + "...";
        return this;
    }

    public static PasteResponse create(Paste paste, Exchange exchange) {
        PasteResponse pasteResponse = new PasteResponse(paste);
        if ("true".equalsIgnoreCase(exchange.query("shorten_content", "false"))) {
            pasteResponse.shortenContent();
        }
        return pasteResponse;
    }
}
