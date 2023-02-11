package de.interaapps.pastefy.controller.publicarea;

import de.interaapps.pastefy.controller.HttpController;
import de.interaapps.pastefy.helper.RequestHelper;
import de.interaapps.pastefy.model.database.Paste;
import de.interaapps.pastefy.model.database.algorithm.PublicPasteEngagement;
import de.interaapps.pastefy.model.responses.paste.PasteResponse;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.httpserver.router.annotation.With;
import org.javawebstack.httpserver.router.annotation.verbs.Get;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.query.Query;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@PathPrefix("/api/v2/public-pastes")
@With("public-pastes-endpoint")
public class PublicPastesController extends HttpController {
    @Get
    public List<PasteResponse> getPublicPastes(Exchange exchange) {
        Query<Paste> query = Repo.get(Paste.class)
                .query()
                .whereExists(PublicPasteEngagement.class, engagementQuery -> engagementQuery.where(Paste.class, "id", "=", PublicPasteEngagement.class, "pasteId").order("score", true))
                .where("visibility", Paste.Visibility.PUBLIC);

        RequestHelper.pagination(query, exchange);
        query.search(exchange.query("search"));
        RequestHelper.queryFilter(query, exchange.getQueryParameters());

        return query
                .stream()
                .map(p -> PasteResponse.create(p, exchange))
                .collect(Collectors.toList());
    }

    @Get("/trending")
    public List<PasteResponse> getTrending(Exchange exchange) {
        Query<Paste> query = Repo.get(Paste.class)
                .query()
                .whereExists(PublicPasteEngagement.class, engagementQuery -> engagementQuery.where(Paste.class, "id", "=", PublicPasteEngagement.class, "pasteId").order("score", true))
                .order("createdAt", true)
                .where("visibility", Paste.Visibility.PUBLIC);
        if ("true".equals(exchange.rawRequest().getParameter("trending"))) {
            Date date = new Date();
            // Check if post is not over 4 Days old
            date.setTime(System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 4));
            query.where("createdAt", ">", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
        }

        RequestHelper.pagination(query, exchange);
        query.search(exchange.query("search"));
        RequestHelper.queryFilter(query, exchange.getQueryParameters());

        return query
                .stream()
                .map(p -> PasteResponse.create(p, exchange))
                .collect(Collectors.toList());
    }

    @Get("/latest")
    public List<PasteResponse> getLatest(Exchange exchange) {
        Query<Paste> query = Repo.get(Paste.class)
                .query()
                .order("createdAt", true)
                .where("visibility", Paste.Visibility.PUBLIC);

        RequestHelper.pagination(query, exchange);
        query.search(exchange.query("search"));
        RequestHelper.queryFilter(query, exchange.getQueryParameters());

        return query
                .stream()
                .map(p -> PasteResponse.create(p, exchange))
                .collect(Collectors.toList());
    }
}
