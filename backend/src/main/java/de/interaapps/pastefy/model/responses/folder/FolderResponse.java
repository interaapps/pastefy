package de.interaapps.pastefy.model.responses.folder;

import de.interaapps.pastefy.model.database.Folder;
import de.interaapps.pastefy.model.responses.paste.PasteResponse;

import java.util.List;
import java.util.stream.Collectors;

public class FolderResponse {
    public boolean exists = false;
    public String id;
    public String name;
    public int userId;
    public List<FolderResponse> children;
    public List<PasteResponse> pastes;

    public FolderResponse(Folder folder, boolean fetchChildren, boolean fetchSubChildren){
        if (folder != null) {
            exists = true;
            name = folder.getName();
            id = folder.getKey();
            userId = folder.getUserId();
            if (fetchChildren) {
                children = folder.getFolders().stream().map(child -> new FolderResponse(child, fetchSubChildren, fetchSubChildren)).collect(Collectors.toList());
                pastes = folder.getPastes().stream().map(PasteResponse::new).collect(Collectors.toList());
            }
        }
    }

    public FolderResponse(Folder folder, boolean showChildren){
        this(folder, showChildren, showChildren);
    }

    public FolderResponse(Folder folder){
        this(folder, true, true);
    }

}
