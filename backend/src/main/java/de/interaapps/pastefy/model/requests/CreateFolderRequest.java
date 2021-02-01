package de.interaapps.pastefy.model.requests;

import org.javawebstack.validator.Rule;

public class CreateFolderRequest {
    @Rule("required|string")
    public String name;
    public String parent;
}
