package de.interaapps.pastefy.auth.strategies;

import org.javawebstack.http.router.HTTPRouter;
import de.interaapps.pastefy.auth.Passport;

public abstract class Strategy {

    protected String name;
    protected Passport passport;

    public abstract void createRoutes(String prefixUrl, HTTPRouter httpRouter);

    public String getName() {
        return name;
    }

    public Passport getPassport() {
        return passport;
    }

    public void setPassport(Passport passport) {
        this.passport = passport;
    }

}
