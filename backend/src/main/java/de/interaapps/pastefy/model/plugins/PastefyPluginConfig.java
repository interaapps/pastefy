package de.interaapps.pastefy.model.plugins;


public class PastefyPluginConfig {
    public String name;
    public String description;
    public String icon;
    public FrontendConfig frontend;
    public Boolean listed = true;

    public class FrontendConfig {
        public String folder;
        public String entrypoint;
    }
}
