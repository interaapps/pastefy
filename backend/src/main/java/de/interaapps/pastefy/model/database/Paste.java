package de.interaapps.pastefy.model.database;

import org.apache.commons.lang3.RandomStringUtils;
import org.javawebstack.orm.Model;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Dates;
import org.javawebstack.orm.annotation.Table;

import java.sql.Timestamp;
import java.util.Base64;

@Dates
@Table("pastes")
public class Paste extends Model {
    @Column
    private int id;

    @Column
    private String key;

    @Column
    private String title;

    @Column
    private String content;
    @Column
    private int userId = -1;

    @Column
    private boolean encrypted = false;

    @Column
    public String folder;

    @Column
    public Timestamp createdAt;

    @Column
    public Timestamp updatedAt;

    public Paste(){
        key = RandomStringUtils.random(8, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890");
    }

    public String getContent() {
        return new String(Base64.getDecoder().decode(content));
    }

    public void setContent(String content) {
        this.content = Base64.getEncoder().encodeToString(content.getBytes());
    }

    public String getTitle() {
        return new String(Base64.getDecoder().decode(title));
    }

    public void setTitle(String content) {
        this.title = Base64.getEncoder().encodeToString(content.getBytes());
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
        return Repo.get(Folder.class).where("key", folder).get();
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder.getKey();
    }
}
