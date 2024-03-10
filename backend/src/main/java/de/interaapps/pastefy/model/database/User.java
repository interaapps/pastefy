package de.interaapps.pastefy.model.database;

import de.interaapps.pastefy.model.responses.folder.FolderResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.javawebstack.orm.Model;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.annotation.*;
import org.javawebstack.passport.strategies.oauth2.OAuth2Provider;
import org.javawebstack.passport.strategies.oauth2.providers.*;

import java.sql.Timestamp;
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
        id = RandomStringUtils.random(8, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890");
        while (Repo.get(User.class).where("id", id).first() != null)
            id = RandomStringUtils.random(8, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890");
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
        return Repo.get(Folder.class).where("userId", id).whereNotNull("parent").all().stream().map(folder -> new FolderResponse(folder, true, true, false, true)).collect(Collectors.toList());
    }

    public void sendNotification(Notification notification) {
        notification.userId = id;
        notification.save();
    }

    public boolean roleCheck() {
        return type != Type.AWAITING_ACCESS && type != Type.BLOCKED;
    }

    public enum AuthenticationProvider {
        INTERAAPPS(InteraAppsOAuth2Provider.class, "interaapps"),
        GOOGLE(GoogleOAuth2Provider.class, "google"),
        GITHUB(GitHubOAuth2Provider.class, "github"),
        TWITCH(TwitchOAuth2Provider.class, "twitch"),
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
}
