package de.interaapps.pastefy.model.responses.paste;

import de.interaapps.pastefy.model.database.PasteAIInfo;
import org.javawebstack.abstractdata.AbstractArray;

public class PasteAiInfoResponse {
    public boolean dangerous = false;
    public String suggestedFilename;
    public AbstractArray warnings;
    public AbstractArray tags;
    public String description;

    public PasteAiInfoResponse(PasteAIInfo pasteAIInfo) {
        dangerous = pasteAIInfo.dangerous;
        suggestedFilename = pasteAIInfo.suggestedFilename;
        warnings = pasteAIInfo.warningsJson;
        tags = pasteAIInfo.tagsJson;
        description = pasteAIInfo.description;
    }
}
