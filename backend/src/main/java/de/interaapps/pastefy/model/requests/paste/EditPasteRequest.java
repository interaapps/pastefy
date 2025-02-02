package de.interaapps.pastefy.model.requests.paste;

import de.interaapps.pastefy.model.database.Paste;

import java.util.List;

public class EditPasteRequest {

    public String title;
    public String content;
    public Boolean encrypted;
    public String folder;
    public Paste.Type type;

    public List<String> tags;

    public Paste.Visibility visibility;

    public String expireAt;
}
