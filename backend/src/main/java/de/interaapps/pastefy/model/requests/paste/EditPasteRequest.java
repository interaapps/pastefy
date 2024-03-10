package de.interaapps.pastefy.model.requests.paste;

import de.interaapps.pastefy.model.database.Paste;
import org.javawebstack.validator.Rule;

import java.util.List;

public class EditPasteRequest {
    @Rule("string")
    public String title;
    @Rule({"string"})
    public String content;
    @Rule("boolean")
    public Boolean encrypted;
    public String folder;
    public Paste.Type type;

    public List<String> tags;

    public Paste.Visibility visibility;

    public String expireAt;
}
