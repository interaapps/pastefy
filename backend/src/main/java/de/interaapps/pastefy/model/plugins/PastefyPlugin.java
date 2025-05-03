package de.interaapps.pastefy.model.plugins;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class PastefyPlugin {
    public PastefyPluginConfig config;
    public PastefyBackendPlugin backendPlugin;
    public String path;

    public PastefyPlugin(String folder) throws IOException {
        this.path = folder;

        File configFile = new File(path + "/plugin.json");
        config = new Gson().fromJson(FileUtils.readFileToString(configFile), PastefyPluginConfig.class);
    }

    public String getPublicFolder() {
        return "/assets/plugins/" + config.name;
    }
}
