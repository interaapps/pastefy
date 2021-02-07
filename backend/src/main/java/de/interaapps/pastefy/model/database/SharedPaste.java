package de.interaapps.pastefy.model.database;

import de.interaapps.pastefy.model.auth.User;
import org.javawebstack.orm.Model;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Dates;
import org.javawebstack.orm.annotation.Table;

import java.sql.Timestamp;

@Dates
@Table("shared_pastes")
public class SharedPaste extends Model {
    @Column
    private int id;

    @Column
    private int userId;

    @Column
    private int targetId;

    @Column
    private String paste;

    @Column
    public Timestamp createdAt;

    @Column
    public Timestamp updatedAt;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUser(User user) {
        this.userId = user.getId();
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public void setTarget(User target) {
        this.targetId = target.getId();
    }

    public Paste getPaste() {
        return Repo.get(Paste.class).where("key", paste).first();
    }

    public void setPaste(String paste) {
        this.paste = paste;
    }

    public void setPaste(Paste paste) {
        this.paste = paste.getKey();
    }
}
