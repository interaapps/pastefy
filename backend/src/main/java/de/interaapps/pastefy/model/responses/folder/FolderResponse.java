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

    public FolderResponse(Folder folder){
        if (folder != null) {
            exists = true;
            name = folder.getName();
            id = folder.getKey();
            userId = folder.getUserId();
            children = folder.getFolders().stream().map(FolderResponse::new).collect(Collectors.toList());
            pastes   = folder.getPastes().stream().map(PasteResponse::new).collect(Collectors.toList());
        }
    }

}
