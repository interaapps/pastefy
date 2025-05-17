package de.interaapps.pastefy.model.elastic;

import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.interaapps.pastefy.Pastefy;
import de.interaapps.pastefy.model.database.Paste;
import de.interaapps.pastefy.model.database.User;
import org.javawebstack.orm.Repo;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ElasticPaste {
    public int id;
    public String key;
    public String title;
    public String content;
    public String userId;
    public String forkedFrom;
    public boolean encrypted = false;
    public String folder;
    public Paste.Type type = Paste.Type.PASTE;
    public Paste.Visibility visibility;
    public Timestamp expireAt = null;
    public Timestamp createdAt;
    public Timestamp updatedAt;
    public Paste.StorageType storageType = Paste.StorageType.DATABASE;
    public Integer version = 0;
    public Integer engagementScore = 0;
    public String[] tags = {};
    public Integer starCount = 0;
    public String[] starredBy = {};

    public ElasticUser user;

    public static ElasticPaste fromPaste(Paste paste) {
        ElasticPaste elasticPaste = new ElasticPaste();
        elasticPaste.id = paste.getId();
        elasticPaste.key = paste.getKey();
        elasticPaste.title = paste.getTitle();
        elasticPaste.content = paste.getContent(false);
        elasticPaste.userId = paste.getUserId();
        elasticPaste.forkedFrom = paste.getForkedFrom();
        elasticPaste.encrypted = paste.isEncrypted();
        elasticPaste.folder = paste.getFolderId();
        elasticPaste.type = paste.getType();
        elasticPaste.visibility = paste.getVisibility();
        elasticPaste.expireAt = paste.expireAt;
        elasticPaste.createdAt = paste.createdAt;
        elasticPaste.updatedAt = paste.updatedAt;
        elasticPaste.storageType = paste.getStorageType();
        elasticPaste.version = paste.getVersion();
        elasticPaste.tags = paste.getTags().toArray(new String[0]);

        elasticPaste.starCount = paste.getStarCounts();
        elasticPaste.starredBy = paste.getStarUsers().stream().map(u -> u.userId).toArray(String[]::new);
        elasticPaste.engagementScore = paste.getEngagementScore();

        if (paste.getUserId() != null) {
            User user1 = paste.getUser();
            if (user1 != null) {
                elasticPaste.user = ElasticUser.fromUser(user1);
            }
        }

        return elasticPaste;
    }

    public static void store(Paste paste) {
        if (Pastefy.getInstance().isElasticsearchEnabled()) {
            System.out.println("Storing to elastic "+ paste.getId());
            try {
                Pastefy.getInstance()
                        .getElasticsearchClient()
                        .index(i -> i
                                .index("pastefy_pastes")
                                .id(String.valueOf(paste.getId()))
                                .document(ElasticPaste.fromPaste(paste))
                        );
            } catch (Exception e) {
                e.printStackTrace();
            }
            paste.setIndexedInElastic(true);
            Map<String, Object> update = new HashMap<>();
            update.put("indexed_in_elastic", paste.isIndexedInElastic());
            Repo.get(Paste.class)
                    .where("id", paste.getId())
                    .update(update);
            System.out.println("Storing to elastic done "+ paste.getId());
        }
    }

    public static void delete(Paste paste) {
        if (Pastefy.getInstance().isElasticsearchEnabled()) {
            try {
                Pastefy.getInstance()
                        .getElasticsearchClient()
                        .delete(d -> d
                            .index("pastefy_pastes")
                            .id(String.valueOf(paste.getId()))
                        );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateEngagement(Paste paste) {
        if (Pastefy.getInstance().isElasticsearchEnabled()) {
            try {
                Pastefy.getInstance()
                        .getElasticsearchClient()
                        .updateByQuery(u -> u
                            .index("pastefy_pastes")
                                .query(q -> q.term(t -> t.field("id").value(String.valueOf(paste.getId()))))
                                .script(s -> s
                                        .source(su -> su.scriptString("ctx._source.engagementScore = params.engagementScore"))
                                        .params("engagementScore", JsonData.of(paste.getEngagementScore()))
                                )
                        );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateTags(Paste paste) {
        if (Pastefy.getInstance().isElasticsearchEnabled()) {
            try {
                Pastefy.getInstance()
                        .getElasticsearchClient()
                        .updateByQuery(u -> u
                            .index("pastefy_pastes")
                                .query(q -> q.term(t -> t.field("id").value(String.valueOf(paste.getId()))))
                                .script(s -> s
                                    .source(su -> su.scriptString("ctx._source.tags = params.tags"))
                                    .params("engagementScore", JsonData.of(paste.getTags()))
                                )
                        );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static void updatePasteUser(Paste paste) {
        if (Pastefy.getInstance().isElasticsearchEnabled()) {
            try {
                Pastefy.getInstance()
                        .getElasticsearchClient()
                        .updateByQuery(u -> u
                            .index("pastefy_pastes")
                                .query(q -> q.term(t -> t.field("id").value(String.valueOf(paste.getId()))))
                                .script(s -> s
                                    .source(su -> su.scriptString("ctx._source.engagementScore = params.engagementScore"))
                                    .params("engagementScore", JsonData.of(paste.getEngagementScore()))
                                )
                        );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
