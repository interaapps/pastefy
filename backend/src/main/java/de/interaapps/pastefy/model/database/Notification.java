package de.interaapps.pastefy.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Dates;
import org.javawebstack.orm.annotation.Table;

import java.sql.Timestamp;

@Dates
@Table("notification")
public class Notification extends Model {
    @Column
    public int id;

    @Column
    private String message;

    @Column
    public int userId;

    @Column
    public String url;

    @Column
    public boolean alreadyRead = false;

    @Column
    public boolean received = false;


    @Column
    public Timestamp createdAt;

    @Column
    public Timestamp updatedAt;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getUrl() {
        return url;
    }

    public void setAlreadyRead(boolean alreadyRead) {
        this.alreadyRead = alreadyRead;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setReceived(boolean received) {
        this.received = received;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
