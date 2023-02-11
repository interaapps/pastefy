package de.interaapps.pastefy.controller;

import de.interaapps.pastefy.model.database.Paste;
import de.interaapps.pastefy.model.database.algorithm.PublicPasteEngagement;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.helper.MimeType;
import org.javawebstack.httpserver.router.annotation.With;
import org.javawebstack.httpserver.router.annotation.params.Path;
import org.javawebstack.httpserver.router.annotation.verbs.Get;
import org.javawebstack.orm.Repo;

public class RawController extends HttpController {

    @Get("/{id}/raw")
    @With("auth-login-required-read")
    public String getPasteRaw(Exchange exchange, @Path("id") String id) {
        Paste paste = Repo.get(Paste.class).where("key", id).first();
        exchange.contentType(MimeType.PLAIN);
        if (paste == null) {
            exchange.status(404);
            return "404 - Paste not found";
        }

        if (paste.isPublic()) {
            PublicPasteEngagement.addInterestFromPaste(paste, 1);
        }

        return paste.getContent();
    }
}
