package de.interaapps.pastefy.model.database;

import org.apache.commons.lang3.RandomStringUtils;
import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Dates;
import org.javawebstack.orm.annotation.Table;

import java.sql.Timestamp;

@Dates
@Table("auth_keys")
public class AuthKey extends Model {
    @Column
    public int id;

    @Column(size = 60)
    private String key;


    @Column(size = 160)
    public String apiKey;

    @Column
    public int userId;

    @Column
    public Type type = Type.USER;

    @Column
    public Timestamp createdAt;

    @Column
    public Timestamp updatedAt;

    public AuthKey() {
        key = RandomStringUtils.random(60, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890");
    }

    public String getKey() {
        return key;
    }


    public enum Type {
        API, USER
    }
}
