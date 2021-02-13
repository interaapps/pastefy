package de.interaapps.pastefy.controller;

import de.interaapps.pastefy.model.database.Paste;
import org.javawebstack.framework.HttpController;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.helper.MimeType;
import org.javawebstack.httpserver.router.annotation.Get;
import org.javawebstack.httpserver.router.annotation.Path;
import org.javawebstack.orm.Repo;

public class RawController extends HttpController {

    @Get("/{id}/raw")
    public String getPasteRaw(Exchange exchange, @Path("id") String id){
        Paste paste = Repo.get(Paste.class).where("key", id).first();
        exchange.contentType(MimeType.PLAIN);
        if (paste == null) {
            exchange.status(404);
            return "404 - Paste not found";
        }
        return paste.getContent();
    }
}
