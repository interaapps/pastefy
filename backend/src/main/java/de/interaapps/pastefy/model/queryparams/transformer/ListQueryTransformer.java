package de.interaapps.pastefy.model.queryparams.transformer;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.util.ObjectBuilder;
import de.interaapps.pastefy.Pastefy;
import de.interaapps.pastefy.helper.AbstractDataHelper;
import de.interaapps.pastefy.model.database.Paste;
import de.interaapps.pastefy.model.queryparams.ListQueryParameters;
import org.javawebstack.abstractdata.AbstractArray;
import org.javawebstack.abstractdata.AbstractElement;
import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.orm.Model;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.query.Query;
import org.javawebstack.orm.query.QueryGroup;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ListQueryTransformer<K extends Model, T, E> {
    private final Repo<K> repo;
    private Class<E> elasticClass;
    private final String elasticIndexName;

    public ListQueryTransformer(Repo<K> repo, Class<E> elasticClass, String elasticIndexName) {
        this.repo = repo;
        this.elasticClass = elasticClass;
        this.elasticIndexName = elasticIndexName;
    }

    protected void applyORMOrder(ListQueryParameters params, Query<K> query, String field, boolean asc) {
        query.order(field, !asc);
    }

    public Query<K> buildORMQuery(ListQueryParameters params) {
        Query<K> query = repo.query();

        if (params.sort != null) {
            String[] sort = params.sort.split(",");
            for (String string : sort) {
                applyORMOrder(params, query, string.replace("+", ""), string.contains("+"));
            }
        } else {
            applyORMOrder(params, query, "createdAt", true);
        }

        if (params.search != null) {
            query.search(params.search);
        }
        if (params.filters != null) {
            query.and(q -> applyFiltersToQuery(q, new AbstractObject().set("$and", params.filters), false));
        }
        return query;
    }

    protected void ormFieldFilter(QueryGroup<K> query, String operator, String key, AbstractElement value, boolean isOrFilter) {
        if (operator.equals("$eq")) {
            if (isOrFilter) query.orWhere(key, value.string());
            else query.where(key, value.string());
        } else if (operator.equals("$ne")) {
            if (isOrFilter) query.orWhere(key, "!=", value.string());
            else query.where(key, "!=", value.string());
        } else if (operator.equals("$null")) {
            if (isOrFilter) query.orWhereNull(key);
            else query.whereNull(key);
        } else if (operator.equals("$notNull")) {
            if (isOrFilter) query.orWhereNotNull(key);
            else query.whereNotNull(key);
        } else if (operator.equals("$gt")) {
            if (isOrFilter) query.orWhere(key, ">", value.string());
            else query.where(key, ">", value.string());
        } else if (operator.equals("$gte")) {
            if (isOrFilter) query.orWhere(key, ">=", value.string());
            else query.where(key, ">=", value.string());
        } else if (operator.equals("$lt")) {
            if (isOrFilter) query.orWhere(key, "<", value.string());
            else query.where(key, "<", value.string());
        } else if (operator.equals("$lte")) {
            if (isOrFilter) query.orWhere(key, "<=", value.string());
            else query.where(key, "<=", value.string());
        }
    }

    private QueryGroup<K> applyFiltersToQuery(QueryGroup<K> query, AbstractObject filters, boolean isOrFilter) {
        filters.forEach((key, value) -> {
            if (key.equals("$and") || key.equals("$or")) {
                value = AbstractDataHelper.ifArrayConvertToArray(value);
                if (!value.isArray())
                    value = new AbstractArray().add(value);


                AbstractElement finalValue = value;
                if (key.equals("$and")) {
                    if (isOrFilter) {
                        query.orWhere(q -> {
                            for (AbstractElement o : finalValue.array())
                                applyFiltersToQuery(q, o.object(), false);
                            return q;
                        });
                    } else {
                        query.where(q -> {
                            for (AbstractElement o : finalValue.array())
                                applyFiltersToQuery(q, o.object(), false);
                            return q;
                        });
                    }
                } else { // $or
                    if (isOrFilter) {
                        query.orWhere(q -> {
                            for (AbstractElement o : finalValue.array())
                                applyFiltersToQuery(q, o.object(), true);
                            return q;
                        });
                    } else {
                        query.where(q -> {
                            for (AbstractElement o : finalValue.array())
                                applyFiltersToQuery(q, o.object(), true);
                            return q;
                        });
                    }
                }
            } else {
                if (!value.isObject()) {
                    value = new AbstractObject().set("$eq", value);
                }
                value.object().forEach((k, v) -> {
                    ormFieldFilter(query, k, key, v, isOrFilter);
                });
            }
        });
        return query;
    }



    public BoolQuery.Builder buildElasticQueryBool(Exchange exchange, ListQueryParameters params, AbstractObject filters, BoolQuery.Builder builder) {
        filters.forEach((key, value) -> {
            if (key.equals("$and") || key.equals("$or")) {
                value = AbstractDataHelper.ifArrayConvertToArray(value);
                if (!value.isArray())
                    value = new AbstractArray().add(value);

                if (key.equals("$and")) {
                    value.array()
                        .forEach(o -> builder
                            .must(must -> must
                                .bool(b -> buildElasticQueryBool(exchange, params, o.object(), b))
                            )
                        );
                } else { // $or
                    value.array()
                            .forEach(o -> builder
                                    .should(must -> must
                                            .bool(b -> buildElasticQueryBool(exchange, params, o.object(), b))
                                    ).minimumShouldMatch("1")
                            );
                }
            } else {
                if (!value.isObject()) {
                    value = new AbstractObject().set("$eq", value);
                }
                value.object().forEach((k, v) -> {
                    if (k.equals("$eq")) {
                        builder.must(m -> m.term(t -> t.field(key).value(v.string())));
                    } else if (k.equals("$ne")) {
                        builder.mustNot(m -> m.term(t -> t.field(key).value(v.string())));
                    } else if (k.equals("$null")) {
                        builder.mustNot(m -> m.exists(e -> e.field(key)));
                    } else if (k.equals("$notNull")) {
                        builder.must(m -> m.exists(e -> e.field(key)));
                    } else if (k.equals("$gt")) {
                        builder.must(m -> m.range(r -> r.term(g -> g.field(key).gt(v.string()))));
                    } else if (k.equals("$gte")) {
                        builder.must(m -> m.range(r -> r.term(g -> g.field(key).gte(v.string()))));
                    } else if (k.equals("$lt")) {
                        builder.must(m -> m.range(r -> r.term(g -> g.field(key).lt(v.string()))));
                    } else if (k.equals("$lte")) {
                        builder.must(m -> m.range(r -> r.term(g -> g.field(key).lte(v.string()))));
                    }
                });
            }
        });
        return builder;
    }

    public ObjectBuilder<BoolQuery> buildElasticSearchQuery(Exchange exchange, ListQueryParameters params, BoolQuery.Builder builder) {
        if (params.search != null && !params.search.trim().isEmpty()) {
            builder.must(m -> m.multiMatch(mm ->
                mm.query(params.search)
                    .fields(getSearchableFields())
                    .fuzziness("2")
                    .prefixLength(0)
                    .maxExpansions(30)
                    .minimumShouldMatch("1")
            ));
        }

        if (params.filters != null && params.filters.size() > 0) {
            // System.out.println(repo.getInfo().getFilterable());
            builder.filter(f -> f.bool(b -> buildElasticQueryBool(exchange, params, new AbstractObject().set("$and", params.filters), b)));
        }
        return builder;
    }


    public List<T> get(Exchange exchange, ListQueryParameters params) {
        if (Pastefy.getInstance().isElasticsearchEnabled()) {
            try {
                List<E> collect = Pastefy.getInstance()
                        .getElasticsearchClient()
                        .search(s -> {
                            s.index(elasticIndexName)
                                    .query(b -> b.bool(bool -> buildElasticSearchQuery(exchange, params, bool)))
                                    .size(params.pageLimit)
                                    .from(params.pageLimit * (params.page - 1));

                            if (params.sort != null) {
                                s.sort(sort -> {
                                    for (String string : params.sort.split(",")) {
                                        sort.field(f -> f.field(string.replace("+", "")).order(string.contains("+") ? SortOrder.Asc : SortOrder.Desc));
                                    }
                                    return sort;
                                });
                            }

                            // System.out.println(s.build().toString());
                            return s;
                        }
                        , elasticClass).hits().hits().stream().map(Hit::source).collect(Collectors.toList());
                return createElasticResponse(exchange, params, collect);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return createResponse(exchange, params, buildORMQuery(params)
                .limit(params.pageLimit)
                .offset(params.pageLimit * (params.page - 1))
                .get());
    }

    public abstract List<T> createResponse(Exchange exchange, ListQueryParameters params, List<K> list);

    public abstract List<T> createElasticResponse(Exchange exchange, ListQueryParameters params, List<E> list);

    public List<String> getSearchableFields() {
        return repo.getInfo().getSearchable();
    }
}
