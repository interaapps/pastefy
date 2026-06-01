package de.interaapps.pastefy.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Dates;
import org.javawebstack.orm.annotation.Table;

import java.sql.Timestamp;

@Dates
@Table("paste_comments")
public class PasteComment extends Model {
    @Column
    private int id;

    @Column(size = 8)
    private String paste;

    @Column(size = 8)
    private String userId;

    @Column(size = 2000)
    private String content;

    @Column
    private Integer parentId;

    @Column
    private Integer lineFrom;

    @Column
    private Integer lineTo;

    @Column
    public Timestamp createdAt;

    @Column
    public Timestamp updatedAt;

    public int getId() {
        return id;
    }

    public String getPaste() {
        return paste;
    }

    public void setPaste(String paste) {
        this.paste = paste;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getLineFrom() {
        return lineFrom;
    }

    public void setLineFrom(Integer lineFrom) {
        this.lineFrom = lineFrom;
    }

    public Integer getLineTo() {
        return lineTo;
    }

    public void setLineTo(Integer lineTo) {
        this.lineTo = lineTo;
    }

    public static PasteComment get(int id) {
        return Repo.get(PasteComment.class).where("id", id).first();
    }
}
