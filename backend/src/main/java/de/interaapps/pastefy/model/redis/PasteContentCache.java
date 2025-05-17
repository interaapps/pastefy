package de.interaapps.pastefy.model.redis;

import de.interaapps.pastefy.Pastefy;
import de.interaapps.pastefy.model.database.Paste;

public class PasteContentCache {
    public static String getCachedContent(Paste paste) {
        if (!Pastefy.getInstance().isRedisEnabled()) return null;
        try {
            return Pastefy.getInstance().getRedisClient().get("paste:" + paste.getId());
        } catch (Exception e) {
            System.out.println("Failed to get cached content for paste " + paste.getId());
            if (Pastefy.getInstance().isDevMode()) e.printStackTrace();
            return null;
        }
    }

    public static void setCachedContent(Paste paste) {
        if (!Pastefy.getInstance().isRedisEnabled()) return;
        try {
            Pastefy.getInstance().getRedisClient()
                    .setex("paste:" + paste.getId(), 60 * 30, paste.getContent(false));
        } catch (Exception e) {
            System.out.println("Failed to set cached content for paste " + paste.getId());
            if (Pastefy.getInstance().isDevMode()) e.printStackTrace();
        }
    }

    public static void deleteCachedContent(Paste paste) {
        if (!Pastefy.getInstance().isRedisEnabled()) return;
        try {
            Pastefy.getInstance().getRedisClient().del("paste:" + paste.getId());
        } catch (Exception e) {
            System.out.println("Failed to delete cached content for paste " + paste.getId());
            e.printStackTrace();
        }
    }
}
