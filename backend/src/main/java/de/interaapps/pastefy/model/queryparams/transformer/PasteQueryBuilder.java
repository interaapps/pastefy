package de.interaapps.pastefy.model.queryparams.transformer;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import co.elastic.clients.util.ObjectBuilder;
import de.interaapps.pastefy.helper.RequestHelper;
import de.interaapps.pastefy.model.database.Paste;
import de.interaapps.pastefy.model.database.PasteStar;
import de.interaapps.pastefy.model.database.User;
import de.interaapps.pastefy.model.database.algorithm.PublicPasteEngagement;
import de.interaapps.pastefy.model.elastic.ElasticPaste;
import de.interaapps.pastefy.model.queryparams.ListQueryParameters;
import de.interaapps.pastefy.model.queryparams.PasteQueryParameters;
import de.interaapps.pastefy.model.responses.paste.PasteResponse;
import org.javawebstack.abstractdata.AbstractElement;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.query.Query;
import org.javawebstack.orm.query.QueryGroup;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PasteQueryBuilder extends ListQueryTransformer<Paste, PasteResponse, ElasticPaste> {
    public PasteQueryBuilder() {
        super(Repo.get(Paste.class), ElasticPaste.class, "pastefy_pastes");
    }

    @Override
    protected void applyORMOrder(ListQueryParameters params, Query<Paste> query, String field, boolean asc) {
        if (field.equals("engagementScore")) {

            Query<PublicPasteEngagement> publicPasteEngagements = Repo.get(PublicPasteEngagement.class)
                    .query()
                    .limit(params.pageLimit)
                    .offset(params.pageLimit * (params.page - 1))
                    .order("score", !asc)
                    .whereExists(Paste.class, q -> q
                            .where(Paste.class, "id", "=", PublicPasteEngagement.class, "pasteId")
                    );

            if (params.filters.has("createdAt")) {
                publicPasteEngagements.where("createdAt", ">", params
                        .filters
                        .get("createdAt").object().string("$gt"));
            }


            List<PublicPasteEngagement> engagements = publicPasteEngagements.all();

            query.whereIn("id", engagements.stream().map(p -> p.pasteId).toArray());
            return;
        }
        super.applyORMOrder(params, query, field, asc);
    }

    @Override
    protected void ormFieldFilter(QueryGroup<Paste> query, String operator, String key, AbstractElement value, boolean isOrFilter) {
        if (key.equals("starredBy")) {
            Function<Query<PasteStar>, Query<PasteStar>> queryQueryFunction = (q) -> q
                    .where(PasteStar.class, "paste", "=", Paste.class, "key")
                    .where("userId", value.string());
            if (isOrFilter) {
                query.orWhereExists(PasteStar.class, queryQueryFunction);
                return;
            }
            query.whereExists(PasteStar.class, queryQueryFunction);
            return;
        }
        super.ormFieldFilter(query, operator, key, value, isOrFilter);
    }

    @Override
    public ObjectBuilder<BoolQuery> buildElasticSearchQuery(Exchange exchange, ListQueryParameters params, BoolQuery.Builder builder) {
        PasteQueryParameters pasteQueryParameters = (PasteQueryParameters) params;
        boolean loggedIn = exchange.attrib("loggedIn");
        User user = exchange.attrib("user");
        System.out.println(pasteQueryParameters.filters.toJsonString());
        if (pasteQueryParameters.filterTags != null && pasteQueryParameters.filterTags.length > 0) {
            builder.must(b -> {
                List<String> tags = Arrays.asList(pasteQueryParameters.filterTags);
                List<FieldValue> tagValues = tags.stream()
                        .map(FieldValue::of)
                        .collect(Collectors.toList());

                b.terms(v -> v
                    .field("tags")
                    .terms(TermsQueryField.of(c -> c.value(tagValues)))
                );

                return b;
            });
        }

        return super.buildElasticSearchQuery(exchange, params, builder);
    }

    @Override
    public List<PasteResponse> createResponse(Exchange exchange, ListQueryParameters params, List<Paste> list) {
        return list
                .stream()
                .map(p -> PasteResponse.create(p, exchange, exchange.attrib("user")))
                .collect(Collectors.toList());
    }

    @Override
    public List<PasteResponse> createElasticResponse(Exchange exchange, ListQueryParameters params, List<ElasticPaste> list) {
        return list.stream().map(p -> {
            PasteResponse pasteResponse = new PasteResponse(p);
            if (((PasteQueryParameters) params).shortenContent)
                pasteResponse.shortenContent();
            return pasteResponse;
        }).collect(Collectors.toList());
    }

    @Override
    public List<String> getSearchableFields() {
        return Arrays.asList("title^3", "content", "user.name", "user.uniqueName");
    }
}
