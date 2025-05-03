package de.interaapps.pastefy.controller.pastes;

import de.interaapps.pastefy.Pastefy;
import de.interaapps.pastefy.controller.HttpController;
import de.interaapps.pastefy.model.database.Paste;
import org.apache.commons.text.StringEscapeUtils;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.router.annotation.params.Path;
import org.javawebstack.http.router.router.annotation.verbs.Get;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class PasteMetaController extends HttpController {
    private String html = null;

    public PasteMetaController() {
        try {
            InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("static/index.html");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buff = new byte[2323];
            int r;
            while ((r = resourceAsStream.read(buff)) != -1) {
                byteArrayOutputStream.write(buff, 0, r);
            }

            html = byteArrayOutputStream.toString();

            String META_START_TAG = "<!--META_TAGS-->";
            String META_END_TAG = "<!--META_TAGS_END-->";

            int startMetaTags = html.indexOf(META_START_TAG);
            int endMetaTags = html.indexOf(META_END_TAG);


            html = html.substring(0, startMetaTags) + "<!--META_TAGS_REPLACEMENT-->" + html.substring(endMetaTags+META_END_TAG.length());
        } catch (Exception e) {}
    }

    @Get("/{paste}")
    public String pasteTags(Exchange exchange, @Path("paste") String pasteId) {
        if (Pastefy.getInstance().getConfig().get("pastefy.metatags", "false").equalsIgnoreCase("true")) return null;
        if (html == null || pasteId.length() != 8) return null;

        if (!isSocialMediaBot(exchange.header("User-Agent"))) return null;

        Paste paste = Paste.get(pasteId);

        if (paste == null || paste.isPrivate() || paste.isEncrypted()) return null;

        Map<String, String> tags = new HashMap<>();
        tags.put("og:site_name", "Pastefy");
        tags.put("og:type", "article");
        tags.put("twitter:card", "summary");

        String title = paste.getTitle().isEmpty() ? "Paste" : paste.getTitle();
        tags.put("og:title", title);
        tags.put("title", title);

        tags.put("og:description", "Share code with Pastefy.");
        tags.put("twitter:description", "Share code with Pastefy");
        tags.put("description", "Share code with Pastefy");

        String url = Pastefy.getInstance().getConfig().get("server.name") + "/" + pasteId;
        tags.put("og:url", url);
        tags.put("twitter:url", url);

        tags.put("og:image", url + "/thumbnail.png");
        tags.put("twitter:image", url + "/thumbnail.png");

        return html.replace("<!--META_TAGS_REPLACEMENT-->", tagsToHTML(tags));
    }


    private String tagsToHTML(Map<String, String> map){
        StringBuilder out = new StringBuilder();
        map.forEach((key, value) -> {
            String name = StringEscapeUtils.escapeHtml4(key);
            out.append("<meta property=\"").append(name).append("\" name=\"").append(name).append("\" content=\"").append(StringEscapeUtils.escapeHtml4(value)).append("\" />");
        });
        return out.toString();
    }

    public static boolean isSocialMediaBot(String userAgent) {
        if (userAgent == null) return false;

        String[] socialMediaBots = {
                "Twitterbot",  // Twitter
                "facebookexternalhit",  // Facebook
                "Instagram",  // Instagram
                "Googlebot",  // Google
                "LinkedInBot",  // LinkedIn
                "Pinterest",  // Pinterest
                "Slackbot",  // Slack
                "WhatsApp"  // WhatsApp
        };

        // Check if userAgent contains any social media bot signature
        for (String bot : socialMediaBots) {
            if (userAgent.contains(bot)) {
                return true;  // Bot found
            }
        }
        return false;  // No bot found
    }
}
