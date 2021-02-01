package de.interaapps.pastefy.model.auth;

import com.google.gson.annotations.SerializedName;

public class IAAuthUser implements User {
    public boolean valid;
    public int id;
    @SerializedName("username")
    public String username;
    @SerializedName("profilepic")
    public String profilePicture;
    @SerializedName("userkey")
    public String userKey;
    public String color;

    @Override
    public int getId() {
        return id;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public String getFavouriteColor() {
        return color;
    }

    public String getName() {
        return username;
    }
}
