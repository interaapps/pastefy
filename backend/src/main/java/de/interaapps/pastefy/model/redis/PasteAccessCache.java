package de.interaapps.pastefy.model.redis;

import de.interaapps.pastefy.Pastefy;
import de.interaapps.pastefy.model.database.Paste;
import redis.clients.jedis.Jedis;

public class PasteAccessCache {
    public static long getAccessCount(Paste paste) {
        if (!Pastefy.getInstance().isRedisEnabled()) return 0;
        try {
            return Long.parseLong(Pastefy.getInstance().getRedisClient().get("paste:" + paste.getId() + ":accessCount"));
        } catch (Exception e) {
            if (Pastefy.getInstance().isDevMode()) e.printStackTrace();
            return 0;
        }
    }

    public static void increaseAccessCount(Paste paste) {
        if (!Pastefy.getInstance().isRedisEnabled()) return;
        Jedis redis = Pastefy.getInstance().getRedisClient();
        try {
            String key = "paste:" + paste.getId() + ":accessCount";

            if (redis.get(key) != null) {
                redis.incr(key);
            } else {
                redis.setex(key, 60 * 30, "1");
            }
        } catch (Exception e) {
            System.out.println("Failed to set access count for paste " + paste.getId());
            if (Pastefy.getInstance().isDevMode()) e.printStackTrace();
        }
    }
}
