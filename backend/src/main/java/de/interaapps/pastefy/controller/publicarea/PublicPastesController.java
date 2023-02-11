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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@PathPrefix("/api/v2/public-pastes")
@With("public-pastes-endpoint")
public class PublicPastesController extends HttpController {
    @Get
    public List<PasteResponse> getPublicPastes(Exchange exchange) {
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

    @Get("/trending")
    public List<PasteResponse> getTrending(Exchange exchange) {
        Query<PublicPasteEngagement> publicPasteEngagements = Repo.get(PublicPasteEngagement.class).query();

        RequestHelper.pagination(publicPasteEngagements, exchange);

        if ("true".equals(exchange.rawRequest().getParameter("trending"))) {
            Date date = new Date();
            date.setTime(System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 4));
            publicPasteEngagements.where("createdAt", ">", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
        }

        Query<Paste> query = Repo.get(Paste.class).query();

        Map<Integer, Integer> scores = new ConcurrentHashMap<>();

        List<PublicPasteEngagement> engagements = publicPasteEngagements.all();

        for (PublicPasteEngagement engagement : engagements) {
            scores.put(engagement.pasteId, engagement.score);
        }

        query.whereIn("id", engagements.stream().map(p -> p.pasteId).toArray());

        query.where("visibility", Paste.Visibility.PUBLIC);

        query.search(exchange.query("search"));
        RequestHelper.queryFilter(query, exchange.getQueryParameters());



        return query
                .stream()
                .sorted(Collections.reverseOrder(Comparator.comparingInt(p -> scores.get(p.getId()))))
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
