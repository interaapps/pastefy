package de.interaapps.pastefy.controller.stats;

import de.interaapps.pastefy.Pastefy;
import de.interaapps.pastefy.controller.HttpController;
import de.interaapps.pastefy.exceptions.PermissionsDeniedException;
import de.interaapps.pastefy.model.database.AuthKey;
import de.interaapps.pastefy.model.database.User;
import de.interaapps.pastefy.model.responses.app.StatsResponse;
import org.javawebstack.http.router.router.annotation.PathPrefix;
import org.javawebstack.http.router.router.annotation.params.Attrib;
import org.javawebstack.http.router.router.annotation.verbs.Get;

@PathPrefix("/api/v2/app/stats")
public class StatsController extends HttpController {
    @Get
    public StatsResponse stats(@Attrib("authkey") AuthKey authKey, @Attrib("user") User user) {
        if (!Pastefy.getInstance().getConfig().get("pastefy.publicstats", "false").equalsIgnoreCase("true") && !authKey.hasPermission("stats:read") && user.type != User.Type.ADMIN)
            throw new PermissionsDeniedException();

        return StatsResponse.create();
    }
}
