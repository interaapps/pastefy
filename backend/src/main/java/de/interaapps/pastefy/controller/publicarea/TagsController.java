package de.interaapps.pastefy.controller.publicarea;

import de.interaapps.pastefy.controller.HttpController;
import de.interaapps.pastefy.exceptions.NotFoundException;
import de.interaapps.pastefy.helper.RequestHelper;
import de.interaapps.pastefy.model.database.algorithm.TagListing;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.router.annotation.PathPrefix;
import org.javawebstack.http.router.router.annotation.With;
import org.javawebstack.http.router.router.annotation.params.Path;
import org.javawebstack.http.router.router.annotation.verbs.Get;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.query.Query;

import java.util.List;

@With("public-pastes-endpoint")
@PathPrefix("/api/v2/public/tags")
public class TagsController extends HttpController {
    @Get
    public List<TagListing> getTags(Exchange exchange) {
        Query<TagListing> query = Repo.get(TagListing.class).query();

        query.order("pasteCount", true);

        RequestHelper.pagination(query, exchange);
        query.search(exchange.query("search"));
        RequestHelper.queryFilter(query, exchange.getQueryParameters());

        return query.all();
    }

    @Get("/{tag}")
    public TagListing getUser(@Path("tag") String tag) {
        return TagListing.getOrCreate(tag);
    }
}
