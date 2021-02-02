package de.interaapps.pastefy.model.requests.paste;

import org.javawebstack.validator.Rule;

public class CreatePasteRequest {
    @Rule("string")
    public String title = "";
    @Rule("required|string")
    public String content;
    @Rule("boolean")
    public boolean encrypted = false;
    public String folder;
}
