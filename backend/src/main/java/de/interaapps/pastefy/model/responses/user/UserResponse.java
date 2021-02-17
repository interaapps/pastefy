package de.interaapps.pastefy.model.responses.user;

import de.interaapps.pastefy.model.auth.User;

public class UserResponse {

    public boolean loggedIn = false;
    public int id;
    public String name;
    public String color;
    public String profilePicture;

    public UserResponse(User user) {
        if (user == null)
            return;

        name = user.getName();
        color = user.getFavouriteColor();
        profilePicture = user.getProfilePicture();
        id = user.getId();
        loggedIn = true;
    }
}
