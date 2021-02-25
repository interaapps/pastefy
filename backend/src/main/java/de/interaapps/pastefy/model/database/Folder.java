package de.interaapps.pastefy.model.database;

import org.apache.commons.lang3.RandomStringUtils;
import org.javawebstack.orm.Model;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Dates;
import org.javawebstack.orm.annotation.Table;

import java.sql.Timestamp;
import java.util.List;

@Dates
@Table("folder")
public class Folder extends Model {
    @Column
    private int id;

    @Column(size = 8)
    private String key;

    @Column
    private String name = "";

    @Column
    private int userId;


    @Column
    private String parent;

    @Column
    public Timestamp createdAt;

    @Column
    public Timestamp updatedAt;

    public Folder() {
        key = RandomStringUtils.random(8, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890");
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

    public List<Paste> getPastes() {
        return Repo.get(Paste.class).where("folder", key).order("updated_at", true).all();
    }

    public List<Folder> getFolders() {
        return Repo.get(Folder.class).where("parent", key).order("updated_at", true).all();
    }

    public void delete() {
        getPastes().forEach(paste -> paste.delete());
        getFolders().forEach(folder -> folder.delete());
        super.delete();
    }

    public Folder getParent() {
        return Repo.get(getClass()).where("key", parent).first();
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public void setParent(Folder parent) {
        this.parent = parent.key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
