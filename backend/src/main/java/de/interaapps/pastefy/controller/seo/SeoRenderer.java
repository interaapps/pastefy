package de.interaapps.pastefy.controller.seo;

import de.interaapps.pastefy.Pastefy;
import org.apache.commons.text.StringEscapeUtils;
import org.javawebstack.http.router.Exchange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SeoRenderer {
    private static final Logger LOGGER = Logger.getLogger(SeoRenderer.class.getName());
    private static final String META_START_TAG = "<!--META_TAGS-->";
    private static final String META_END_TAG = "<!--META_TAGS_END-->";
    private static final String META_REPLACEMENT = "<!--META_TAGS_REPLACEMENT-->";
    private static final String TITLE_REPLACEMENT = "<!--TITLE_REPLACEMENT-->";
    private static final String SEO_CONTENT_REPLACEMENT = "<!--SEO_CONTENT_REPLACEMENT-->";
    private static final Pattern TITLE_TAG_PATTERN = Pattern.compile("(?is)<title\\b[^>]*>.*?</title>");
    private static final Pattern APP_MOUNT_PATTERN = Pattern.compile("(?is)(<div\\s+id=[\"']app[\"'][^>]*>)\\s*</div>");
    private static final Pattern HTTP_SCHEME_PATTERN = Pattern.compile("(?i)^https?://");
    private static final Pattern ANY_SCHEME_PATTERN = Pattern.compile("(?i)^[a-z][a-z0-9+.-]*://");

    private final String html;

    public SeoRenderer() {
        Pastefy pastefy = Pastefy.getInstance();
        if (pastefy != null && pastefy.getHomeHTML() != null) {
            String preparedHtml = prepareHtml(pastefy.getHomeHTML());
            if (preparedHtml != null) {
                html = preparedHtml;
                return;
            }
        }

        html = loadHtml();
    }

    public String render(Exchange exchange, Supplier<SeoPage> pageSupplier) {
        if (!isEnabled() || html == null) return null;

        SeoPage page = pageSupplier.get();
        if (page == null) return null;

        exchange.header("Content-Type", "text/html; charset=UTF-8");
        return html
                .replace(TITLE_REPLACEMENT, escapeHtml(page.title))
                .replace(META_REPLACEMENT, tagsToHTML(page))
                .replace(SEO_CONTENT_REPLACEMENT, page.content);
    }

    public SeoPage page(String canonicalPath, String title, String description) {
        return new SeoPage(absoluteUrl(canonicalPath), title, description);
    }

    public String absoluteUrl(String pathOrUrl) {
        if (pathOrUrl == null || pathOrUrl.trim().isEmpty()) return getBaseUrl();

        String value = pathOrUrl.trim();
        if (HTTP_SCHEME_PATTERN.matcher(value).find()) return value;
        return getBaseUrl() + (value.startsWith("/") ? value : "/" + value);
    }

    public String pathSegment(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    public String truncate(String value, int maxLength) {
        int codePointCount = value.codePointCount(0, value.length());
        if (codePointCount <= maxLength) return value;

        int endIndex = value.offsetByCodePoints(0, maxLength - 3);
        return value.substring(0, endIndex) + "...";
    }

    public String truncateWithoutEllipsis(String value, int maxLength) {
        int codePointCount = value.codePointCount(0, value.length());
        if (codePointCount <= maxLength) return value;
        return value.substring(0, value.offsetByCodePoints(0, maxLength));
    }

    public String escapeHtml(String value) {
        return StringEscapeUtils.escapeHtml4(value == null ? "" : value);
    }

    private boolean isEnabled() {
        Pastefy pastefy = Pastefy.getInstance();
        return pastefy != null
                && pastefy.getConfig().get("pastefy.metatags", "false").equalsIgnoreCase("true");
    }

    private String loadHtml() {
        try (InputStream resourceStream = getClass().getClassLoader().getResourceAsStream("static/index.html")) {
            if (resourceStream == null) {
                LOGGER.warning("Unable to load static/index.html for SEO metadata");
                return null;
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = resourceStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return prepareHtml(new String(outputStream.toByteArray(), StandardCharsets.UTF_8));
        } catch (IOException | RuntimeException exception) {
            LOGGER.log(Level.WARNING, "Unable to prepare SEO metadata HTML", exception);
            return null;
        }
    }

    private String prepareHtml(String sourceHtml) {
        int startMetaTags = sourceHtml.indexOf(META_START_TAG);
        int endMetaTags = sourceHtml.indexOf(META_END_TAG, startMetaTags + META_START_TAG.length());
        if (startMetaTags < 0 || endMetaTags < 0) {
            LOGGER.warning("Unable to find SEO metadata placeholders in static/index.html");
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

    private String tagsToHTML(SeoPage page) {
        StringBuilder out = new StringBuilder();
        appendMetaTags(out, "name", page.seoTags);
        out.append("<link rel=\"canonical\" href=\"").append(escapeHtml(page.canonicalUrl)).append("\" />");
        appendMetaTags(out, "property", page.openGraphTags);
        appendMetaTags(out, "name", page.twitterTags);
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

    public class SeoPage {
        private final String canonicalUrl;
        private final String title;
        private final Map<String, String> seoTags = new LinkedHashMap<>();
        private final Map<String, String> openGraphTags = new LinkedHashMap<>();
        private final Map<String, String> twitterTags = new LinkedHashMap<>();
        private String content = "";

        private SeoPage(String canonicalUrl, String title, String description) {
            this.canonicalUrl = canonicalUrl;
            this.title = title;

            meta("description", description);
            openGraph("og:site_name", "Pastefy");
            type("website");
            openGraph("og:title", title);
            openGraph("og:description", description);
            openGraph("og:url", canonicalUrl);
            twitter("twitter:card", "summary");
            twitter("twitter:title", title);
            twitter("twitter:description", description);
            twitter("twitter:url", canonicalUrl);
        }

        public SeoPage meta(String name, String value) {
            if (value != null) seoTags.put(name, value);
            return this;
        }

        public SeoPage openGraph(String name, String value) {
            if (value != null) openGraphTags.put(name, value);
            return this;
        }

        public SeoPage twitter(String name, String value) {
            if (value != null) twitterTags.put(name, value);
            return this;
        }

        public SeoPage type(String type) {
            return openGraph("og:type", type);
        }

        public SeoPage image(String pathOrUrl) {
            if (pathOrUrl == null || pathOrUrl.trim().isEmpty()) return this;

            String imageUrl = absoluteUrl(pathOrUrl);
            openGraph("og:image", imageUrl);
            twitter("twitter:card", "summary_large_image");
            twitter("twitter:image", imageUrl);
            return this;
        }

        public SeoPage content(String content) {
            this.content = content == null ? "" : content;
            return this;
        }
    }
}
