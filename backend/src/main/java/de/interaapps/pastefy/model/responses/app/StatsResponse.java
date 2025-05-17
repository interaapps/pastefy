package de.interaapps.pastefy.model.responses.app;

import de.interaapps.pastefy.model.database.Paste;
import org.javawebstack.orm.Repo;

public class StatsResponse {

    private int createdPastes;
    private int loggedInPastes;

    public static StatsResponse create() {
        StatsResponse response = new StatsResponse();

        response.createdPastes = Repo.get(Paste.class).count();
        response.loggedInPastes = Repo.get(Paste.class).query().whereNotNull("userId").count();

        return response;
    }
}
