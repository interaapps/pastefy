package de.interaapps.pastefy.model.database;

import de.interaapps.pastefy.exceptions.PermissionsDeniedException;
import org.apache.commons.lang3.RandomStringUtils;
import org.javawebstack.abstractdata.AbstractArray;
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

    @Column(size = 255)
    public String accessToken;

    @Column(size = 255)
    public String refreshToken;

    @Column(size = 8)
    public String userId;

    @Column
    public Type type = Type.USER;

    @Column
    private AbstractArray scopes;

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

    public boolean hasPermission(String permission){
        if (permission.equals(""))
            return true;

        return hasScope(permission) || hasScope(permission.split(":")[0]);
    }

    /**
     * Standard: group.permission:action
     *      or pastefy.ga|paste:read (Will passthrough the request but adds a user-id header)
     * */
    public void checkPermission(String ...permissions){
        for (String permission : permissions) {
            if (hasPermission(permission))
                return;
        }
        throw new PermissionsDeniedException();
    }

    public AuthKey addScope(String scope){
        if (scopes == null)
            scopes = new AbstractArray();
        scopes.add(scope);
        return this;
    }

    private boolean hasScope(String scope){
        if (type != Type.ACCESS_TOKEN)
            return true;

        if (scopes == null)
            return false;
        return scopes.stream().anyMatch(scope1 -> scope1.string().equals(scope));
    }

    public enum Type {
        API, USER, ACCESS_TOKEN
    }
}
