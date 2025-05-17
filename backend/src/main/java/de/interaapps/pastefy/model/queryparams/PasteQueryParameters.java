package de.interaapps.pastefy.model.queryparams;

import de.interaapps.pastefy.model.database.User;
import org.javawebstack.abstractdata.AbstractArray;
import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.http.router.Exchange;

import java.util.function.Consumer;

public class PasteQueryParameters extends ListQueryParameters {
    public String[] filterTags = new String[0];
    public boolean shortenContent = false;

    public PasteQueryParameters(Exchange exchange, Consumer<PasteQueryParameters> function) {
        super(exchange, (f) -> function.accept((PasteQueryParameters) f));
        if (exchange.getQueryParameters().has("filter_tags")) {
            filterTags = exchange.getQueryParameters().get("filter_tags").string().split(",");
        }

        if (exchange.query("shorten_content") != null)
            shortenContent = exchange.query("shorten_content", Boolean.class);
    }

    public static PasteQueryParameters fromWithDefaults(Exchange exchange, Consumer<PasteQueryParameters> function) {
        return new PasteQueryParameters(exchange, function);
    }
    public static PasteQueryParameters from(Exchange exchange) {
        return new PasteQueryParameters(exchange, (a) -> a.sort = "createdAt");
    }


    public ListQueryParameters guarded(Exchange exchange) {
        boolean loggedIn = exchange.attrib("loggedIn");
        User user = exchange.attrib("user");

        System.out.println("adding guard");
        AbstractArray orFilters = new AbstractArray();
        if (user == null || !user.isAdmin()) {
            if (user != null) {
                orFilters.add(new AbstractObject().set("userId", user.getId()));
                orFilters.add(new AbstractObject().set("starredBy", user.getId()));
            }
            orFilters.add(new AbstractObject().set("visibility", "PUBLIC"));
        }
        filters.set("$or", orFilters);

        return this;
    }
}
