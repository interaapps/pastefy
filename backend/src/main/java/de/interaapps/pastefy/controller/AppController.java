package de.interaapps.pastefy.controller;

import de.interaapps.pastefy.Pastefy;
import de.interaapps.pastefy.model.responses.app.AppInfoResponse;
import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.httpserver.router.annotation.verbs.Get;

@PathPrefix("/api/v2/app")
public class AppController extends HttpController {
    @Get("/info")
    public AppInfoResponse appInfo() {
        return new AppInfoResponse(Pastefy.getInstance());
    }
}
