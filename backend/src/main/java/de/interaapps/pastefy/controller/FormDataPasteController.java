package de.interaapps.pastefy.controller;

import com.google.gson.Gson;
import de.interaapps.pastefy.model.database.Paste;
import de.interaapps.pastefy.model.responses.paste.MultiPastesElement;
import org.javawebstack.framework.HttpController;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.router.annotation.verbs.Post;
import org.javawebstack.webutils.IO;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;

public class FormDataPasteController extends HttpController {
    @Post("/")
    public String createPaste(Exchange exchange) throws ServletException, IOException {
        exchange.enableMultipart("/tmp", 4000000, 1048576);
        Paste paste = new Paste();

        Map<String, String> files = new HashMap<>();
        exchange.rawRequest().getParts().forEach(part -> {
            try {
                files.put(part.getSubmittedFileName(), IO.readTextStream(part.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        if (files.size() > 0) {
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
            return "https://"+exchange.header("Host")+"/"+paste.getKey()+"\n";
        }

        return "No file given. curl -F 'f=@filename' pastefy.ga\n";
    }
}
