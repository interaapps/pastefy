package de.interaapps.pastefy.model.database;

import de.interaapps.pastefy.Pastefy;
import de.interaapps.pastefy.exceptions.NotFoundException;
import de.interaapps.pastefy.exceptions.PastePrivateException;
import de.interaapps.pastefy.model.database.algorithm.PublicPasteEngagement;
import de.interaapps.pastefy.model.database.algorithm.TagListing;
import de.interaapps.pastefy.model.elastic.ElasticPaste;
import de.interaapps.pastefy.model.minio.MinioPaste;
import de.interaapps.pastefy.model.redis.PasteAccessCache;
import de.interaapps.pastefy.model.redis.PasteContentCache;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.errors.*;
import org.javawebstack.abstractdata.AbstractArray;
import org.javawebstack.abstractdata.AbstractElement;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.orm.Model;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.annotation.*;
import org.javawebstack.webutils.util.IO;
import org.javawebstack.webutils.util.RandomUtil;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// @Dates
@Table("pastes")
public class Paste extends Model {
    @Column(size = 8)
    @Searchable
    @Filterable
    public String folder;
    @Column
    public Timestamp expireAt = null;
    @Column
    @Searchable
    public Timestamp createdAt;
    @Column
    public Timestamp updatedAt;
    protected String cachedContents = null;
    @Column
    private int id;
    @Column(size = 8)
    @Searchable
    @Filterable
    private String key;
    @Column
    @Searchable
    @Filterable
    private String title;
    @Column(size = 16777215)
    @Searchable
    private String content;
    @Column(size = 8)
    @Filterable
    private String userId;
    @Column(size = 8)
    @Filterable
    private String forkedFrom;
    @Column
    @Filterable
    private boolean encrypted = false;
    @Column
    @Searchable
    @Filterable
    private Type type = Type.PASTE;
    @Column
    @Filterable
    private Visibility visibility = Visibility.UNLISTED;
    @Column
    private StorageType storageType = StorageType.DATABASE;
    @Column
    private Integer version = 0;
    @Column
    private Boolean indexedInElastic = false;

    public Paste() {
        key = RandomUtil.string(8);
    }

    public static Paste get(String pasteKey) {
        return Repo.get(Paste.class).where("key", pasteKey).first();
    }

    public static Paste getAccessiblePasteOrFail(String pasteKey, User user) {
        Paste paste = Repo.get(Paste.class).where("key", pasteKey).first();
        if (paste == null) throw new NotFoundException();

        if (paste.isPrivate() && (user == null || !Objects.equals(user.id, paste.getUserId()))) {
            throw new PastePrivateException();
        }

        return paste;
    }

    public String getRawContent() {
        return content;
    }

    public void writeResponse(Exchange exchange) throws IOException {
        if (Pastefy.getInstance().isRedisEnabled()) {
            PasteAccessCache.increaseAccessCount(this);

            if (PasteAccessCache.getAccessCount(this) > 10) {
                PasteContentCache.setCachedContent(this);
            }
            String cachedContent = PasteContentCache.getCachedContent(this);
            if (cachedContent != null) {
                exchange.write(cachedContent);
                return;
            };
        }

        if (Pastefy.getInstance().isMinioEnabled() && storageType == StorageType.S3) {
            exchange.write(MinioPaste.get(this));
            return;
        }
        exchange.write(content);
    }

    public String getContent(boolean withCache) {
        if (withCache) {
            PasteAccessCache.increaseAccessCount(this);

            if (PasteAccessCache.getAccessCount(this) > 10) {
                PasteContentCache.setCachedContent(this);
            }
            String cachedContent = PasteContentCache.getCachedContent(this);
            if (cachedContent != null) return cachedContent;
        }

        if (Pastefy.getInstance().isMinioEnabled() && storageType == StorageType.S3) {
            if (cachedContents == null) {
                GetObjectResponse response = MinioPaste.get(this);
                try {
                    cachedContents = IO.readTextStream(response);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return cachedContents;
        }

        return content;
    }

    public String getContent() {
        return getContent(true);
    }

    public void setContent(String content) {
        this.content = content;
        this.storageType = StorageType.DATABASE;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String content) {
        this.title = content;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public User getUser() {
        return User.get(userId);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void addTag(String tag) {
        PasteTag pTag = new PasteTag();
        pTag.paste = key;
        pTag.tag = tag;
        pTag.save();
        TagListing.updateCount(pTag.tag);
        Pastefy.getInstance().executeAsync(() -> ElasticPaste.updateTags(this));
    }

    public Visibility getVisibility() {
        return visibility == null ? Visibility.UNLISTED : visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public boolean isPublic() {
        return visibility == Visibility.PUBLIC;
    }

    public boolean isPrivate() {
        return visibility == Visibility.PRIVATE;
    }

    public String getForkedFrom() {
        return forkedFrom;
    }

    public void setForkedFrom(String forkedFrom) {
        this.forkedFrom = forkedFrom;
    }

    public String getKey() {
        return key;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public Folder getFolder() {
        return Repo.get(Folder.class).where("key", folder).first();
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder.getKey();
    }

    public String getFolderId() {
        return folder;
    }

    public void setExpireAt(String timeString) {
        this.expireAt = Timestamp.valueOf(timeString);
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public List<String> getTags() {
        return Repo.get(PasteTag.class).where("paste", key).get().stream().map(t -> t.tag).collect(Collectors.toList());
    }

    public int getStarCounts() {
        return Repo.get(PasteStar.class)
                .where("paste", key)
                .count();
    }

    public List<PasteStar> getStarUsers() {
        return Repo.get(PasteStar.class)
                .where("paste", key)
                .all();
    }

    public int getEngagementScore() {
        PublicPasteEngagement pasteEngagement = Repo.get(PublicPasteEngagement.class)
                .where("pasteId", id).first();
        if (pasteEngagement == null) return 0;
        return pasteEngagement.score;
    }

    public AbstractArray getMultiPasteParts() {
        return AbstractElement.fromJson(getContent()).array();
    }

    public Boolean isIndexedInElastic() {
        return indexedInElastic != null && indexedInElastic;
    }

    public void setIndexedInElastic(Boolean indexedInElastic) {
        this.indexedInElastic = indexedInElastic;
    }

    @Override
    public void save() {
        if (version == null) version = 1;
        version++;

        indexedInElastic = false;

        if (createdAt == null) {
            createdAt = Timestamp.from(Instant.now());
        } else {
            PasteContentCache.deleteCachedContent(this);
        }

        updatedAt = Timestamp.from(Instant.now());

        super.save();

        if (Pastefy.getInstance().isElasticsearchEnabled()) {
            Pastefy.getInstance().executeAsync(() -> MinioPaste.store(this));
        }

        if (Pastefy.getInstance().isElasticsearchEnabled()) {
            Pastefy.getInstance().executeAsync(() -> ElasticPaste.store(this));
        }
    }

    public void superSave() {
        super.save();
    }

    @Override
    public void delete() {
        Repo.get(PasteTag.class).where("paste", key).delete();
        Repo.get(PublicPasteEngagement.class).where("pasteId", id).delete();

        if (storageType == StorageType.S3) {
            Pastefy.getInstance().executeAsync(() -> MinioPaste.delete(this));
        }
        if (indexedInElastic != null && indexedInElastic) {
            Pastefy.getInstance().executeAsync(() -> ElasticPaste.delete(this));
        }

        super.delete();
        PasteContentCache.deleteCachedContent(this);
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }

    public Integer getVersion() {
        return version;
    }

    public String getCachedContents() {
        return cachedContents;
    }

    public void setCachedContents(String cachedContents) {
        this.cachedContents = cachedContents;
    }

    public enum Type {
        PASTE,
        MULTI_PASTE
    }


    public enum Visibility {
        UNLISTED,
        PUBLIC,
        PRIVATE
    }

    public enum StorageType {
        DATABASE,
        S3,
        HTTP
    }
}
