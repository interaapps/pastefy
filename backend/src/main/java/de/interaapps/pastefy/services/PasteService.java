package de.interaapps.pastefy.services;

import de.interaapps.pastefy.model.queryparams.ListQueryParameters;
import de.interaapps.pastefy.model.queryparams.PasteQueryParameters;
import de.interaapps.pastefy.model.queryparams.transformer.PasteQueryBuilder;
import de.interaapps.pastefy.model.responses.paste.PasteResponse;
import org.javawebstack.http.router.Exchange;

import java.util.List;

public class PasteService {
    public static List<PasteResponse> getAllPastes(Exchange exchange, ListQueryParameters params, boolean guarded) {
        if (guarded) params.guarded(exchange);
        return new PasteQueryBuilder().get(exchange, params);
    }
    public static List<PasteResponse> getAllPastes(Exchange exchange, ListQueryParameters params) {
        return getAllPastes(exchange, params, true);
    }
    public static List<PasteResponse> getAllPastes(Exchange exchange) {
        return getAllPastes(exchange, PasteQueryParameters.from(exchange));
    }
}
