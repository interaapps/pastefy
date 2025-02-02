package de.interaapps.pastefy.controller;

import com.google.gson.Gson;
import de.interaapps.pastefy.model.database.Paste;
import de.interaapps.pastefy.model.responses.paste.MultiPastesElement;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.router.annotation.With;
import org.javawebstack.http.router.router.annotation.verbs.Post;
import org.javawebstack.webutils.util.IO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormDataPasteController extends HttpController {

    @Post("/")
    @With({"rate-limiter", "auth-login-required-create", "awaiting-access-check", "blocked-check"})
    public String createPaste(Exchange exchange) throws IOException {
        exchange.enableMultipart(new File("/tmp"));
        Paste paste = new Paste();

        Map<String, String> files = new HashMap<>();
        exchange.getParts().forEach(part -> {
            try {
                files.put(part.getName(), IO.readTextStream(part.getContentStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        if (!files.isEmpty()) {
            if (files.size() == 1) {
                files.forEach((name, contents) -> {
                    paste.setContent(contents);
                    paste.setTitle(name);
                    paste.setType(Paste.Type.PASTE);
                });

            } else {
                paste.setType(Paste.Type.MULTI_PASTE);
                List<MultiPastesElement> multiPastesElements = new ArrayList<>();
                files.forEach((name, contents) -> {
                    multiPastesElements.add(new MultiPastesElement(name, contents));
                });
                paste.setContent(new Gson().toJson(multiPastesElements));
            }

            paste.save();
            return "https://" + exchange.header("Host") + "/" + paste.getKey() + "\n";
        }

        return "No file given. curl -F 'f=@filename' pastefy.app\n";
    }

}
