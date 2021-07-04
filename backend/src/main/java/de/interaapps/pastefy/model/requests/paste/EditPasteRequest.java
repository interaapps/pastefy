package de.interaapps.pastefy.model.requests.paste;

import de.interaapps.pastefy.model.database.Paste;
import org.javawebstack.validator.Rule;

public class EditPasteRequest {
    @Rule("string")
    public String title;
    @Rule({"string"})
    public String content;
    @Rule("boolean")
    public Boolean encrypted;
    public String folder;
    public Paste.Type type;
}
