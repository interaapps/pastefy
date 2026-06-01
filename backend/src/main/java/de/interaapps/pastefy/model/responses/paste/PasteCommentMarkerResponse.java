package de.interaapps.pastefy.model.responses.paste;

import de.interaapps.pastefy.model.responses.user.PublicUserResponse;

import java.util.ArrayList;
import java.util.List;

public class PasteCommentMarkerResponse {
    public int line;
    public List<PublicUserResponse> profiles = new ArrayList<>();
    public int additionalProfiles;

    public PasteCommentMarkerResponse(int line) {
        this.line = line;
    }
}
