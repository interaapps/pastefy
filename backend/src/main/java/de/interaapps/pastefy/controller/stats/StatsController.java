package de.interaapps.pastefy.controller.stats;

import de.interaapps.pastefy.Pastefy;
import de.interaapps.pastefy.controller.HttpController;
import de.interaapps.pastefy.exceptions.PermissionsDeniedException;
import de.interaapps.pastefy.model.database.AuthKey;
import de.interaapps.pastefy.model.responses.app.StatsResponse;
import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.httpserver.router.annotation.params.Attrib;
import org.javawebstack.httpserver.router.annotation.verbs.Get;

@PathPrefix("/api/v2/app/stats")
public class StatsController extends HttpController {
    @Get
    public StatsResponse stats(@Attrib("authkey") AuthKey authKey){
        if (!Pastefy.getInstance().getConfig().get("pastefy.publicstats", "false").equalsIgnoreCase("true") && !authKey.hasPermission("stats:read"))
            throw new PermissionsDeniedException();

        return StatsResponse.create();
    }
}
