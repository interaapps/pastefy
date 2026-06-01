package de.interaapps.pastefy.model.database;

import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Dates;
import org.javawebstack.orm.annotation.Table;

import java.sql.Timestamp;

@Dates
@Table("background_jobs")
public class BackgroundJob extends Model {
    @Column(id = true, ai = false, size = 255)
    public String key;

    @Column
    public Type type;

    @Column
    public int entityId;

    @Column
    public int sourceVersion;

    @Column
    public int promptVersion;

    @Column
    public Status status = Status.PENDING;

    @Column
    public int attempts = 0;

    @Column
    public Timestamp availableAt;

    @Column
    public Timestamp leaseUntil;

    @Column(size = 64)
    public String leaseToken;

    @Column(size = 2048)
    public String lastError;

    @Column
    public Timestamp createdAt;

    @Column
    public Timestamp updatedAt;

    public enum Type {
        PASTE_AI_INFO
    }

    public enum Status {
        PENDING,
        RUNNING,
        DONE,
        FAILED
    }
}
