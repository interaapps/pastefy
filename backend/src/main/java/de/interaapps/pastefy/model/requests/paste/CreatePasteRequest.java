package de.interaapps.pastefy.model.requests.paste;

import de.interaapps.pastefy.model.database.Paste;
import org.javawebstack.validator.Rule;

public class CreatePasteRequest {
    @Rule("string")
    public String title = "";
    @Rule({"required", "string"})
    public String content;
    @Rule("boolean")
    public boolean encrypted = false;
    public String folder;

    public String expireAt;

    public String forkedFrom;

    public String[] tags = null;

    public Paste.Visibility visibility = Paste.Visibility.UNLISTED;

    public Paste.Type type = Paste.Type.PASTE;
}
