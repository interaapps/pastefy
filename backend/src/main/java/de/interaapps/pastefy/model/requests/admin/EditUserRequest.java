package de.interaapps.pastefy.model.requests.admin;

import de.interaapps.pastefy.model.database.User;
import org.javawebstack.validator.rule.StringRule;

public class EditUserRequest {

    @StringRule(min = 2, max = 255)
    public String name;

    @StringRule(min = 2, max = 33)
    public String uniqueName;

    public User.Type type;
}
