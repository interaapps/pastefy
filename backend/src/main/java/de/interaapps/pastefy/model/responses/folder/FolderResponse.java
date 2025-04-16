package de.interaapps.pastefy.model.responses.folder;

import de.interaapps.pastefy.model.database.Folder;
import de.interaapps.pastefy.model.responses.paste.PasteResponse;

import java.util.List;
import java.util.stream.Collectors;

public class FolderResponse {
    public boolean exists = false;
    public String id;
    public String name;
    public String userId;
    public List<FolderResponse> children;
    public List<PasteResponse> pastes;
    public String created = "0000-00-00 00:00:00";

    public FolderResponse(Folder folder, boolean fetchChildren, boolean fetchSubChildren, boolean fetchPastes, boolean showPrivate) {
        if (folder != null) {
            exists = true;
            name = folder.getName();
            id = folder.getKey();
            userId = folder.getUserId();
            created = folder.createdAt.toString();
            if (fetchChildren) {
                children = folder.getFolders().stream().map(child -> new FolderResponse(child, fetchSubChildren, fetchSubChildren, fetchPastes, showPrivate)).collect(Collectors.toList());
                if (fetchPastes)
                    pastes = folder.getPastes(showPrivate).stream().map(p -> new PasteResponse(p, null, false, false)).collect(Collectors.toList());
            }
        }
    }

    public FolderResponse(Folder folder, boolean showChildren, boolean fetchSubChildren) {
        this(folder, showChildren, fetchSubChildren, true, false);
    }

    public FolderResponse(Folder folder, boolean showChildren) {
        this(folder, showChildren, showChildren);
    }

    public FolderResponse(Folder folder) {
        this(folder, true, true);
    }

}
