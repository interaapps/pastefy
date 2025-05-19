package de.interaapps.pastefy.model.database;

import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import co.elastic.clients.json.JsonData;
import de.interaapps.pastefy.Pastefy;
import de.interaapps.pastefy.auth.strategies.oauth2.OAuth2Provider;
import de.interaapps.pastefy.auth.strategies.oauth2.providers.*;
import de.interaapps.pastefy.model.database.algorithm.PublicPasteEngagement;
import de.interaapps.pastefy.model.elastic.ElasticPaste;
import de.interaapps.pastefy.model.elastic.ElasticStars;
import de.interaapps.pastefy.model.responses.folder.FolderResponse;
import org.javawebstack.orm.Model;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.annotation.*;
import org.javawebstack.webutils.util.RandomUtil;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Dates
@Table("users")
public class User extends Model {

    @Column(size = 8, id = true)
    public String id;

    @Column
    @Searchable
    public String name;

    @Column(size = 33)
    @Filterable
    public String uniqueName;

    @Column(name = "email")
    @Filterable
    public String eMail;

    @Column
    public String avatar;

    @Column(size = 455)
    @Filterable
    public String authId;

    @Column
    @Filterable
    @Searchable
    public AuthenticationProvider authProvider;

    @Column
    @Filterable
    @Searchable
    public Type type = Type.USER;

    @Column
    public Timestamp createdAt;

    @Column
    public Timestamp updatedAt;

    public User() {
        do {
            id = RandomUtil.string(8);
        } while (Repo.get(User.class).where("id", id).isNotEmpty());
    }

    public List<Paste> getPastes() {
        return Repo.get(Paste.class).where("userId", id).all();
    }

    public List<Folder> getFolders() {
        return Repo.get(Folder.class).where("userId", id).all();
    }

    public List<FolderResponse> getFolderTree(boolean fetchChildren, boolean fetchSubChildren, boolean fetchPastes) {
        return Repo.get(Folder.class)
                .where("userId", id)
                .whereNull("parent")
                .all()
                .stream()
                .map(folder -> new FolderResponse(folder, fetchChildren, fetchSubChildren, fetchPastes, true))
                .collect(Collectors.toList());
    }

    public List<FolderResponse> getFolderWithChildren() {
        return Repo.get(Folder.class).where("userId", id).whereNull("parent").all().stream().map(folder -> new FolderResponse(folder, true, true, false, true)).collect(Collectors.toList());
    }

    public void sendNotification(Notification notification) {
        notification.userId = id;
        notification.save();
    }

    public boolean roleCheck() {
        return type != Type.AWAITING_ACCESS && type != Type.BLOCKED;
    }

    public static User get(String id) {
        return Repo.get(User.class).get(id);
    }

    public static User getByName(String name) {
        return Repo.get(User.class).where("uniqueName", name).first();
    }

    public void star(Paste paste) {
        if (!hasStarred(paste)) {
            PasteStar pasteStar = new PasteStar();
            pasteStar.paste = paste.getKey();
            pasteStar.userId = id;
            pasteStar.save();

            Pastefy.getInstance().executeAsync(() -> {
                ElasticStars.addStarCount(paste, this);
                PublicPasteEngagement.addInterestFromPaste(paste, 20);
            });
        }
    }

    public void unstar(Paste paste) {
        Repo.get(PasteStar.class)
                .where("paste", paste.getKey())
                .where("userId", id)
                .delete();

        Pastefy.getInstance().executeAsync(() -> {
            ElasticStars.removeStarCount(paste, this);
            PublicPasteEngagement.addInterestFromPaste(paste, -20);
        });
    }

    public boolean hasStarred(Paste paste) {
        return Repo.get(PasteStar.class)
                .where("paste", paste.getKey())
                .where("userId", id)
                .count() > 0;
    }

    public enum AuthenticationProvider {
        INTERAAPPS(InteraAppsOAuth2Provider.class, "interaapps"),
        GOOGLE(GoogleOAuth2Provider.class, "google"),
        GITHUB(GitHubOAuth2Provider.class, "github"),
        TWITCH(TwitchOAuth2Provider.class, "twitch"),
        OIDC(CustomOAuth2Provider.class, "oidc"),
        DISCORD(DiscordOAuth2Provider.class, "discord");

        private Class<? extends OAuth2Provider> oauth2Service;
        private String name;


        AuthenticationProvider(Class<? extends OAuth2Provider> oauth2Service, String name) {
            this.name = name;
            this.oauth2Service = oauth2Service;
        }

        public Class<? extends OAuth2Provider> getOAuth2ServiceClass() {
            return oauth2Service;
        }

        public static AuthenticationProvider getProviderByClass(Class<? extends OAuth2Provider> oauth2ServiceClass) {
            for (AuthenticationProvider authenticationProvider : AuthenticationProvider.values()) {
                if (oauth2ServiceClass == authenticationProvider.getOAuth2ServiceClass())
                    return authenticationProvider;
            }
            return null;
        }

        public String getName() {
            return name;
        }
    }

    public enum Type {
        USER,
        ADMIN,
        BLOCKED,
        AWAITING_ACCESS
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getAuthId() {
        return authId;
    }

    public AuthenticationProvider getAuthProvider() {
        return authProvider;
    }

    public String geteMail() {
        return eMail;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public boolean isAdmin() {
        return type == Type.ADMIN;
    }


    public void delete() {
        Repo.get(Paste.class).where("userId", id).get().forEach(Paste::delete);
        Repo.get(Folder.class).where("userId", id).delete();
        Repo.get(AuthKey.class).where("userId", id).delete();
        Repo.get(Notification.class).where("userId", id).delete();
        Repo.get(SharedPaste.class).where("targetId", id).orWhere("userId", id).delete();
        super.delete();
    }
}
