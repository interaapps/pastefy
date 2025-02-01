package de.interaapps.pastefy.auth;

import org.javawebstack.http.router.HTTPRouter;
import de.interaapps.pastefy.auth.strategies.Strategy;

import java.util.HashMap;
import java.util.Map;

public class Passport {

    private Map<String, Strategy> strategies = new HashMap<>();
    private String prefixUrl = "";

    public Passport() {}

    public Passport(String prefixUrl){
        this.prefixUrl = prefixUrl;
    }

    public Passport use(String name, Strategy strategy){
        strategy.setPassport(this);
        strategies.put(name, strategy);
        return this;
    }

    public Strategy get(String name){
        return strategies.get(name);
    }

    public Passport createRoutes(HTTPRouter router){
        strategies.forEach((name, strategy) -> {
            strategy.createRoutes(prefixUrl+"/"+name, router);
        });
        return this;
    }

    public String getPrefixUrl() {
        return prefixUrl;
    }
}
