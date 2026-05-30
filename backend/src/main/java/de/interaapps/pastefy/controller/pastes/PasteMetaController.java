package de.interaapps.pastefy.controller.pastes;

import de.interaapps.pastefy.Pastefy;
import de.interaapps.pastefy.controller.HttpController;
import de.interaapps.pastefy.model.database.Paste;
import de.interaapps.pastefy.model.database.User;
import org.apache.commons.text.StringEscapeUtils;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.router.annotation.params.Path;
import org.javawebstack.http.router.router.annotation.verbs.Get;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasteMetaController extends HttpController {
    private static final Logger LOGGER = Logger.getLogger(PasteMetaController.class.getName());
    private static final String META_START_TAG = "<!--META_TAGS-->";
    private static final String META_END_TAG = "<!--META_TAGS_END-->";
    private static final String META_REPLACEMENT = "<!--META_TAGS_REPLACEMENT-->";
    private static final String TITLE_REPLACEMENT = "<!--TITLE_REPLACEMENT-->";
    private static final String SEO_CONTENT_REPLACEMENT = "<!--SEO_CONTENT_REPLACEMENT-->";
    private static final String DEFAULT_TITLE = "Paste";
    private static final int DESCRIPTION_MAX_LENGTH = 180;
    private static final int DEFAULT_PREVIEW_LENGTH = 4096;
    private static final int MAX_PREVIEW_LENGTH = 16384;
    private static final Pattern TITLE_TAG_PATTERN = Pattern.compile("(?is)<title\\b[^>]*>.*?</title>");
    private static final Pattern APP_MOUNT_PATTERN = Pattern.compile("(?is)(<div\\s+id=[\"']app[\"'][^>]*>)\\s*</div>");
    private static final Pattern VALID_PASTE_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{8}$");
    private static final Pattern HTTP_SCHEME_PATTERN = Pattern.compile("(?i)^https?://");
    private static final Pattern ANY_SCHEME_PATTERN = Pattern.compile("(?i)^[a-z][a-z0-9+.-]*://");

    private String html = null;

    public PasteMetaController() {
        Pastefy pastefy = Pastefy.getInstance();
        if (pastefy != null && pastefy.getHomeHTML() != null) {
            html = prepareHtml(pastefy.getHomeHTML());
        }

        if (html != null) return;

        try (InputStream resourceStream = getClass().getClassLoader().getResourceAsStream("static/index.html")) {
            if (resourceStream == null) {
                LOGGER.warning("Unable to load static/index.html for paste metadata");
                return;
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = resourceStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            html = prepareHtml(new String(outputStream.toByteArray(), StandardCharsets.UTF_8));
        } catch (IOException | RuntimeException exception) {
            LOGGER.log(Level.WARNING, "Unable to prepare paste metadata HTML", exception);
        }
    }

    @Get("/{paste}")
    public String pasteTags(Exchange exchange, @Path("paste") String pasteId) {
        if (!Pastefy.getInstance().getConfig().get("pastefy.metatags", "false").equalsIgnoreCase("true")) return null;
        if (html == null || pasteId == null || !VALID_PASTE_ID_PATTERN.matcher(pasteId).matches()) return null;

        Paste paste = Paste.get(pasteId);
        if (paste == null || paste.isPrivate() || paste.isEncrypted()) return null;

        String title = getTitle(paste);
        SeoAuthor author = paste.isPublic() ? getAuthor(paste) : null;
        String description = getDescription(title, author);
        String canonicalUrl = getBaseUrl() + "/" + pasteId;
        String imageUrl = canonicalUrl + "/thumbnail.png";

        Map<String, String> seoTags = new LinkedHashMap<>();
        seoTags.put("description", description);
        if (author != null) seoTags.put("author", author.displayName + " (@" + author.username + ")");

        Map<String, String> openGraphTags = new LinkedHashMap<>();
        openGraphTags.put("og:site_name", "Pastefy");
        openGraphTags.put("og:type", "article");
        openGraphTags.put("og:title", title);
        openGraphTags.put("og:description", description);
        openGraphTags.put("og:url", canonicalUrl);
        openGraphTags.put("og:image", imageUrl);
        if (author != null) openGraphTags.put("article:author", author.profileUrl);

        Map<String, String> twitterTags = new LinkedHashMap<>();
        twitterTags.put("twitter:card", "summary_large_image");
        twitterTags.put("twitter:title", title);
        twitterTags.put("twitter:description", description);
        twitterTags.put("twitter:url", canonicalUrl);
        twitterTags.put("twitter:image", imageUrl);
        if (author != null) twitterTags.put("twitter:creator", "@" + author.username);

        String seoContent = "";
        if (paste.isPublic()) {
            seoContent = getSeoContent(paste, title, author);
            if (seoContent == null) return null;
        }

        exchange.header("Content-Type", "text/html; charset=UTF-8");
        return html
                .replace(TITLE_REPLACEMENT, escapeHtml(title))
                .replace(META_REPLACEMENT, tagsToHTML(seoTags, openGraphTags, twitterTags, canonicalUrl))
                .replace(SEO_CONTENT_REPLACEMENT, seoContent);
    }

    private String prepareHtml(String sourceHtml) {
        int startMetaTags = sourceHtml.indexOf(META_START_TAG);
        int endMetaTags = sourceHtml.indexOf(META_END_TAG, startMetaTags + META_START_TAG.length());
        if (startMetaTags < 0 || endMetaTags < 0) {
            LOGGER.warning("Unable to find paste metadata placeholders in static/index.html");
            return null;
        }

        String preparedHtml = sourceHtml.substring(0, startMetaTags)
                + META_REPLACEMENT
                + sourceHtml.substring(endMetaTags + META_END_TAG.length());

        Matcher appMountMatcher = APP_MOUNT_PATTERN.matcher(preparedHtml);
        if (!appMountMatcher.find()) {
            LOGGER.warning("Unable to find the Vue app mount element in static/index.html");
            return null;
        }
        preparedHtml = appMountMatcher.replaceFirst(
                Matcher.quoteReplacement(appMountMatcher.group(1) + SEO_CONTENT_REPLACEMENT + "</div>")
        );

        Matcher titleMatcher = TITLE_TAG_PATTERN.matcher(preparedHtml);
        if (titleMatcher.find()) {
            return titleMatcher.replaceFirst(Matcher.quoteReplacement("<title>" + TITLE_REPLACEMENT + "</title>"));
        }

        int endHead = preparedHtml.indexOf("</head>");
        if (endHead < 0) {
            LOGGER.warning("Unable to find a head element in static/index.html");
            return null;
        }

        return preparedHtml.substring(0, endHead)
                + "<title>" + TITLE_REPLACEMENT + "</title>"
                + preparedHtml.substring(endHead);
    }

    private String tagsToHTML(
            Map<String, String> seoTags,
            Map<String, String> openGraphTags,
            Map<String, String> twitterTags,
            String canonicalUrl
    ) {
        StringBuilder out = new StringBuilder();
        appendMetaTags(out, "name", seoTags);
        out.append("<link rel=\"canonical\" href=\"").append(escapeHtml(canonicalUrl)).append("\" />");
        appendMetaTags(out, "property", openGraphTags);
        appendMetaTags(out, "name", twitterTags);
        return out.toString();
    }

    private void appendMetaTags(StringBuilder out, String attribute, Map<String, String> tags) {
        tags.forEach((key, value) -> out
                .append("<meta ")
                .append(attribute)
                .append("=\"")
                .append(escapeHtml(key))
                .append("\" content=\"")
                .append(escapeHtml(value))
                .append("\" />"));
    }

    private String getBaseUrl() {
        String baseUrl = Pastefy.getInstance().getConfig().get("server.name", "http://localhost");
        if (baseUrl == null || baseUrl.trim().isEmpty()) return "http://localhost";

        baseUrl = baseUrl.trim();
        if (!HTTP_SCHEME_PATTERN.matcher(baseUrl).find()) {
            baseUrl = ANY_SCHEME_PATTERN.matcher(baseUrl).replaceFirst("");
            baseUrl = "https://" + baseUrl;
        }

        int schemeEnd = baseUrl.indexOf("://") + 3;
        String scheme = baseUrl.substring(0, schemeEnd);
        String remainder = baseUrl.substring(schemeEnd)
                .replaceFirst("^/+", "")
                .replaceAll("/{2,}", "/")
                .replaceFirst("/+$", "");

        return remainder.isEmpty() ? "http://localhost" : scheme + remainder;
    }

    private String getTitle(Paste paste) {
        String title = paste.getTitle();
        if (title == null || title.trim().isEmpty()) return DEFAULT_TITLE;
        return truncate(title.trim().replaceAll("\\s+", " "), 120);
    }

    private String getDescription(String title, SeoAuthor author) {
        if (DEFAULT_TITLE.equals(title)) {
            return author == null ? "View this paste on Pastefy." : "View this paste by @" + author.username + " on Pastefy.";
        }
        if (author != null) {
            return truncate("View \"" + title + "\" by @" + author.username + " on Pastefy.", DESCRIPTION_MAX_LENGTH);
        }
        return truncate("View \"" + title + "\" on Pastefy.", DESCRIPTION_MAX_LENGTH);
    }

    private SeoAuthor getAuthor(Paste paste) {
        if (paste.getUserId() == null) return null;

        User user = paste.getUser();
        if (user == null || user.getUniqueName() == null || user.getUniqueName().trim().isEmpty()) return null;

        String username = user.getUniqueName().trim();
        String displayName = user.getName() == null || user.getName().trim().isEmpty()
                ? username
                : user.getName().trim();
        String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8).replace("+", "%20");
        return new SeoAuthor(displayName, username, getBaseUrl() + "/@" + encodedUsername);
    }

    private String getSeoContent(Paste paste, String title, SeoAuthor author) {
        String content;
        try {
            content = paste.getContent(false);
        } catch (RuntimeException exception) {
            LOGGER.log(Level.WARNING, "Unable to load paste content for SEO preview", exception);
            return null;
        }

        int previewLength = Pastefy.getInstance().getConfig().getInt("pastefy.metatags.previewlength", DEFAULT_PREVIEW_LENGTH);
        previewLength = Math.max(0, Math.min(previewLength, MAX_PREVIEW_LENGTH));

        String preview = content == null ? "" : truncateWithoutEllipsis(content, previewLength);
        String authorHtml = author == null
                ? ""
                : "<p>By <a href=\"" + escapeHtml(author.profileUrl) + "\">"
                + escapeHtml(author.displayName) + " (@" + escapeHtml(author.username) + ")</a></p>";
        return "<main id=\"seo-content\">"
                + "<h1 title=\"paste-title\">" + escapeHtml(title) + "</h1>"
                + authorHtml
                + "<p>View and share code snippets on Pastefy.</p>"
                + "<pre><code>" + escapeHtml(preview) + "</code></pre>"
                + "</main>";
    }

    private String truncate(String value, int maxLength) {
        int codePointCount = value.codePointCount(0, value.length());
        if (codePointCount <= maxLength) return value;

        int endIndex = value.offsetByCodePoints(0, maxLength - 3);
        return value.substring(0, endIndex) + "...";
    }

    private String truncateWithoutEllipsis(String value, int maxLength) {
        int codePointCount = value.codePointCount(0, value.length());
        if (codePointCount <= maxLength) return value;
        return value.substring(0, value.offsetByCodePoints(0, maxLength));
    }

    private String escapeHtml(String value) {
        return StringEscapeUtils.escapeHtml4(value == null ? "" : value);
    }

    private static class SeoAuthor {
        private final String displayName;
        private final String username;
        private final String profileUrl;

        private SeoAuthor(String displayName, String username, String profileUrl) {
            this.displayName = displayName;
            this.username = username;
            this.profileUrl = profileUrl;
        }
    }
}
