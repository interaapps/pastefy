package de.interaapps.pastefy.model.requests.paste;

import org.javawebstack.validator.Rule;

public class AddFriendToPasteRequest {
    @Rule("required|string")
    public String friend;
}
