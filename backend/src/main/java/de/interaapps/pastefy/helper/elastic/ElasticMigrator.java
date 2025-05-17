package de.interaapps.pastefy.helper.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.util.ObjectBuilder;
import de.interaapps.pastefy.Pastefy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ElasticMigrator {
    private ElasticsearchClient client;
    public ElasticMigrator(ElasticsearchClient client) {
        this.client = client;
    }

    public Map<String, Map<String,  Function<Property.Builder, ObjectBuilder<Property>>>> builders = new HashMap<>();

    {
        Map<String,  Function<Property.Builder, ObjectBuilder<Property>>> pastes = new HashMap<>();

        pastes.put("key", p -> p.keyword(k -> k));
        pastes.put("title", p -> p.text(t -> t.fields("keyword", f -> f.keyword(k -> k))));
        pastes.put("content", p -> p.text(t -> t));

        pastes.put("version", p -> p.integer(t -> t));
        pastes.put("starCount", p -> p.integer(k -> k));
        pastes.put("engagementScore", p -> p.integer(k -> k));

        pastes.put("userId", p -> p.keyword(t -> t));
        pastes.put("forkedFrom", p -> p.keyword(k -> k));
        pastes.put("visibility", p -> p.keyword(k -> k));
        pastes.put("folder", p -> p.keyword(k -> k));
        pastes.put("type", p -> p.keyword(k -> k));
        pastes.put("storageType", p -> p.keyword(k -> k));

        pastes.put("tags", p -> p.keyword(k -> k));
        pastes.put("starredBy", p -> p.keyword(k -> k));

        pastes.put("encrypted", p -> p.boolean_(k -> k));

        pastes.put("expireAt", p -> p.date(d -> d));
        pastes.put("createdAt", p -> p.date(d -> d));
        pastes.put("updatedAt", p -> p.date(d -> d));

        pastes.put("user", p -> p.object(t ->
                t
                .properties("id", f -> f.keyword(c -> c))
                .properties("type", f -> f.keyword(c -> c))
                .properties("authId", f -> f.keyword(c -> c))
                .properties("authProvider", f -> f.keyword(c -> c))

                .properties("avatar", f -> f.text(c -> c))

                .properties("name", f -> f.text(c -> c.fields("keyword", f2 -> f2.keyword(k -> k))))
                .properties("uniqueName", f -> f.text(c -> c.fields("keyword", f2 -> f2.keyword(k -> k))))
                .properties("eMail", f -> f.text(c -> c.fields("keyword", f2 -> f2.keyword(k -> k))))

                .properties("createdAt", f -> f.date(c -> c))
                .properties("updatedAt", f -> f.date(c -> c))
            )
        );

        builders.put("pastefy_pastes", pastes);
    }

    public void createIfNotExists(String index) throws IOException {
        if (!Pastefy.getInstance().isIndexAvailable(index)) {
            client.indices().create(c -> c.index(index).mappings(p -> {
                builders.get(index).forEach(p::properties);
                return p;
            }));
        } else {
            client.indices().putMapping(c -> {
                c.index(index);

                builders.get(index).forEach(c::properties);

                return c;
            });
        }
    }

    public void migrateAll() throws IOException {
        for (String index : Pastefy.getInstance().INDEXES) {
            createIfNotExists(index);
        }
    }
}
