package de.interaapps.pastefy.model.requests.paste;

import de.interaapps.pastefy.model.database.Paste;
import org.javawebstack.validator.rule.RequiredRule;

import java.util.List;

public class CreatePasteRequest {

    public String title = "";
    @RequiredRule
    public String content;
    public boolean encrypted = false;
    public String folder;

    public String expireAt;

    public String forkedFrom;

    public List<String> tags = null;

    public Paste.Visibility visibility = Paste.Visibility.UNLISTED;

    public Paste.Type type = Paste.Type.PASTE;

    public boolean ai = false;
}
