package de.interaapps.pastefy.model.plugins;

import de.interaapps.pastefy.Pastefy;
import org.javawebstack.webutils.config.Mapping;

public abstract class PastefyBackendPlugin {
    public PastefyBackendPlugin(Pastefy pastefy) {}

    public abstract void init();
    public abstract void setupModels();
    public abstract void setupPassport();
    public abstract void setupServer();
    public abstract void beforeRoutes();
    public void customConfigMapping(Mapping mapping) {}
}
