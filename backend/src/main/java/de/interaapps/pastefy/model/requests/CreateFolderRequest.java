package de.interaapps.pastefy.model.requests;

import org.javawebstack.validator.rule.RequiredRule;

public class CreateFolderRequest {

    @RequiredRule
    public String name;
    public String parent;

}
