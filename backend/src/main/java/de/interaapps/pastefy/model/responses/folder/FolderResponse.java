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

    public FolderResponse(Folder folder, boolean fetchChildren, boolean fetchSubChildren, boolean fetchPastes) {
        if (folder != null) {
            exists = true;
            name = folder.getName();
            id = folder.getKey();
            userId = folder.getUserId();
            if (fetchChildren) {
                children = folder.getFolders().stream().map(child -> new FolderResponse(child, fetchSubChildren, fetchSubChildren, fetchPastes)).collect(Collectors.toList());
                if (fetchPastes)
                    pastes = folder.getPastes().stream().map(PasteResponse::new).collect(Collectors.toList());
            }
        }
    }

    public FolderResponse(Folder folder, boolean showChildren, boolean fetchSubChildren) {
        this(folder, showChildren, fetchSubChildren, true);
    }

    public FolderResponse(Folder folder, boolean showChildren) {
        this(folder, showChildren, showChildren);
    }

    public FolderResponse(Folder folder) {
        this(folder, true, true);
    }

}
