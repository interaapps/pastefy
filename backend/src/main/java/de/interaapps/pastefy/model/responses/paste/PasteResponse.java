package de.interaapps.pastefy.model.responses.paste;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import de.interaapps.pastefy.model.database.Paste;

public class PasteResponse {
    public boolean exists = false;
    public String id;
    public String content;
    public String title;
    public boolean encrypted = false;
    public String folder;
    public int userId;
    @SerializedName("raw_url")
    public String rawURL;
    public String created = "0000-00-00 00:00:00";

    public PasteResponse(Paste paste) {
        if (paste == null) {
            return;
        }
        id = paste.getKey();
        rawURL = "/"+id+"/raw";
        title = paste.getTitle();
        content = paste.getContent();
        created = paste.createdAt.toString();
        encrypted = paste.isEncrypted();
        userId = paste.getUserId();
        if (paste.getFolderId() != null)
            folder = paste.getFolderId();
        exists = true;
    }

    public PasteResponse shortenContent() {
        if (content.length() > 450)
            content = content.substring(0, 450);
        return this;
    }
}
