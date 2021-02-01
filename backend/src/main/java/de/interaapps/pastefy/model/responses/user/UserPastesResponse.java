package de.interaapps.pastefy.model.responses.user;

import de.interaapps.pastefy.model.responses.folder.FolderResponse;
import de.interaapps.pastefy.model.responses.paste.PasteResponse;

import java.util.List;

public class UserPastesResponse {
    public List<PasteResponse> pastes;
    public List<FolderResponse> folder;
}
