package de.interaapps.pastefy.model.requests.paste;

import org.javawebstack.validator.rule.RequiredRule;

public class AddFriendToPasteRequest {

    @RequiredRule
    public String friend;

}
