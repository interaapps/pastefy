package de.interaapps.pastefy.model.auth;

import de.interaapps.pastefy.model.database.Folder;
import de.interaapps.pastefy.model.database.Notification;
import de.interaapps.pastefy.model.database.Paste;
import de.interaapps.pastefy.model.responses.folder.FolderResponse;
import org.javawebstack.orm.Repo;

import java.util.List;
import java.util.stream.Collectors;

public interface User {
    int getId();

    String getProfilePicture();

    String getFavouriteColor();

    String getName();

    default List<Paste> getPastes() {
        return Repo.get(Paste.class).where("userId", getId()).all();
    }

    default List<Folder> getFolders() {
        return Repo.get(Folder.class).where("userId", getId()).all();
    }

    default List<FolderResponse> getFolderTree(boolean fetchChildren, boolean fetchSubChildren, boolean fetchPastes) {
        return Repo.get(Folder.class).where("userId", getId()).isNull("parent").all().stream().map(folder -> new FolderResponse(folder, fetchChildren, fetchSubChildren, fetchPastes)).collect(Collectors.toList());
    }

    default List<FolderResponse> getFolderWithChildren() {
        return Repo.get(Folder.class).where("userId", getId()).isNull("parent").all().stream().map(folder -> new FolderResponse(folder, true, true, false)).collect(Collectors.toList());
    }

    default void sendNotification(Notification notification) {
        notification.userId = getId();
        notification.save();
    }
}
