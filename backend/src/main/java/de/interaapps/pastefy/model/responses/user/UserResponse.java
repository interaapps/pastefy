package de.interaapps.pastefy.model.responses.user;

import de.interaapps.pastefy.Pastefy;
import de.interaapps.pastefy.model.database.User;
import org.javawebstack.passport.AuthService;

import java.util.List;
import java.util.stream.Collectors;

public class UserResponse {

    public boolean loggedIn = false;
    public String id;
    public String name;
    public String color;
    public String profilePicture;
    public String authType;
    public List<String> authTypes = Pastefy.getInstance().getoAuth2Module().getServices().stream().map(AuthService::getName).collect(Collectors.toList());

    public UserResponse(User user) {
        if (user == null)
            return;

        name = user.getName();
        authType = user.getAuthProvider().getName();
        // color = user.getFavouriteColor();
        color = "#f52966";
        profilePicture = user.getAvatar();
        id = user.getId();
        loggedIn = true;
    }
}
