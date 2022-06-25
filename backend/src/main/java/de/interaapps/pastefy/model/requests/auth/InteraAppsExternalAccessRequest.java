package de.interaapps.pastefy.model.requests.auth;

import java.util.ArrayList;
import java.util.List;

public class InteraAppsExternalAccessRequest {
    public String provider;
    public String appId;
    public String appSecret;
    public String scope;
    public List<String> scopeList = new ArrayList<>();
    public List<String> appScopeList = new ArrayList<>();
    public String userId;
}
