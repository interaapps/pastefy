package de.interaapps.pastefy.model.responses.app;

import de.interaapps.pastefy.Pastefy;
import org.javawebstack.webutils.config.Config;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AppInfoResponse {

    public String customLogo;
    public String customName;
    public boolean aiEnabled = false;
    public Map<String, String> customFooter;
    public boolean loginRequiredForRead;
    public boolean loginRequiredForCreate;

    public boolean encryptionIsDefault;
    public boolean publicPastesEnabled;

    public AppInfoResponse(Pastefy pastefy) {
        Config config = pastefy.getConfig();

        if (config.has("pastefy.info.custom.logo"))
            customLogo = config.get("pastefy.info.custom.logo");

        if (config.has("pastefy.info.custom.name"))
            customName = config.get("pastefy.info.custom.name");

        aiEnabled = pastefy.aiEnabled();

        encryptionIsDefault = config.get("pastefy.encryption.default", "false").toLowerCase(Locale.ROOT).equals("true");

        loginRequiredForRead = pastefy.isLoginRequiredForRead();
        loginRequiredForCreate = pastefy.isLoginRequiredForCreate();

        publicPastesEnabled = pastefy.publicPastesEnabled();

        String footerString = config.get("pastefy.info.custom.footer", "");
        customFooter = new HashMap<>();
        for (String str : footerString.split(",")) {
            if (str.contains("=")) {
                String[] split = str.split("=");
                customFooter.put(split[0], split[1]);
            }
        }
    }
}
