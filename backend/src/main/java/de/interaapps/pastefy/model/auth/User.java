package de.interaapps.pastefy.model.auth;

import de.interaapps.pastefy.model.database.Folder;
import de.interaapps.pastefy.model.database.Notification;
import de.interaapps.pastefy.model.database.Paste;
import org.javawebstack.orm.Repo;

import java.util.List;

public interface User {
    int getId();
    String getProfilePicture();
    String getFavouriteColor();
    String getName();

    default List<Paste> getPastes(){
        return Repo.get(Paste.class).where("userId", getId()).all();
    }

    default List<Folder> getFolders(){
        return Repo.get(Folder.class).where("userId", getId()).all();
    }

    default void sendNotification(Notification notification){
        notification.userId = getId();
        notification.save();
    }
}
