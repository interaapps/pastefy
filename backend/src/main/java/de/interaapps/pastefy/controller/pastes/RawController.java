package de.interaapps.pastefy.controller.pastes;

import de.interaapps.pastefy.Pastefy;
import de.interaapps.pastefy.controller.HttpController;
import de.interaapps.pastefy.exceptions.PastePrivateException;
import de.interaapps.pastefy.model.database.Paste;
import de.interaapps.pastefy.model.database.User;
import de.interaapps.pastefy.model.database.algorithm.PublicPasteEngagement;
import org.javawebstack.abstractdata.AbstractElement;
import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.router.annotation.With;
import org.javawebstack.http.router.router.annotation.params.Attrib;
import org.javawebstack.http.router.router.annotation.params.Path;
import org.javawebstack.http.router.router.annotation.verbs.Get;
import org.javawebstack.http.router.util.MimeType;
import org.javawebstack.orm.Repo;

import java.io.IOException;
import java.util.Objects;

public class RawController extends HttpController {

    @Get("/{id}/raw")
    @With("auth-login-required-read")
    public String getPasteRaw(Exchange exchange, @Path("id") String id, @Attrib("user") User user) {
        Paste paste = Repo.get(Paste.class).where("key", id).first();
        exchange.contentType("text/plain; charset=utf-8");
        if (paste == null) {
            exchange.status(404);
            return "404 - Paste not found";
        }


        if (paste.isPrivate() && (user == null || !Objects.equals(user.id, paste.getUserId()))) {
            throw new PastePrivateException();
        }

        if (paste.isPublic()) {
            Pastefy.getInstance().executeAsync(() -> PublicPasteEngagement.addInterestFromPaste(paste, 1));
        }

        if (exchange.query("part") != null && paste.getType() == Paste.Type.MULTI_PASTE) {
            AbstractObject part = paste.getMultiPasteParts()
                    .stream()
                    .map(AbstractElement::object)
                    .filter(p -> exchange.query("part").equals(p.string("name")))
                    .findFirst()
                    .get();
            if (part.has("contents")) {
                return part.get("contents").string();
            }
        }

        try {
            paste.writeResponse(exchange);
        } catch (IOException e) {
            return "Internal Server Error";
        }
        return "";
    }
}
