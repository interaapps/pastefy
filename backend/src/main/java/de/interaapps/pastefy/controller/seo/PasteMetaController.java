package de.interaapps.pastefy.controller.seo;

import de.interaapps.pastefy.Pastefy;
import de.interaapps.pastefy.controller.HttpController;
import de.interaapps.pastefy.model.database.Paste;
import de.interaapps.pastefy.model.database.User;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.router.annotation.params.Path;
import org.javawebstack.http.router.router.annotation.verbs.Get;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class PasteMetaController extends HttpController {
    private static final Logger LOGGER = Logger.getLogger(PasteMetaController.class.getName());
    private static final String DEFAULT_TITLE = "Paste";
    private static final int DESCRIPTION_MAX_LENGTH = 180;
    private static final int DEFAULT_PREVIEW_LENGTH = 4096;
    private static final int MAX_PREVIEW_LENGTH = 16384;
    private static final Pattern VALID_PASTE_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{8}$");

    private final SeoRenderer seo = new SeoRenderer();

    @Get("/{paste}")
    public String pasteTags(Exchange exchange, @Path("paste") String pasteId) {
        return seo.render(exchange, () -> {
            if (pasteId == null || !VALID_PASTE_ID_PATTERN.matcher(pasteId).matches()) return null;

            Paste paste = Paste.get(pasteId);
            if (paste == null || paste.isPrivate() || paste.isEncrypted()) return null;

            String title = getTitle(paste);
            SeoAuthor author = paste.isPublic() ? getAuthor(paste) : null;
            String description = getDescription(title, author);
            String path = "/" + pasteId;

            String seoContent = "";
            if (paste.isPublic()) {
                seoContent = getSeoContent(paste, title, author);
                if (seoContent == null) return null;
            }

            SeoRenderer.SeoPage page = seo.page(path, title, description)
                    .type("article")
                    .image(path + "/thumbnail.png")
                    .content(seoContent);

            if (author != null) {
                page.meta("author", author.displayName + " (@" + author.username + ")")
                        .openGraph("article:author", author.profileUrl)
                        .twitter("twitter:creator", "@" + author.username);
            }

            return page;
        });
    }

    private String getTitle(Paste paste) {
        String title = paste.getTitle();
        if (title == null || title.trim().isEmpty()) return DEFAULT_TITLE;
        return seo.truncate(title.trim().replaceAll("\\s+", " "), 120);
    }

    private String getDescription(String title, SeoAuthor author) {
        if (DEFAULT_TITLE.equals(title)) {
            return author == null ? "View this paste on Pastefy." : "View this paste by @" + author.username + " on Pastefy.";
        }
        if (author != null) {
            return seo.truncate("View \"" + title + "\" by @" + author.username + " on Pastefy.", DESCRIPTION_MAX_LENGTH);
        }
        return seo.truncate("View \"" + title + "\" on Pastefy.", DESCRIPTION_MAX_LENGTH);
    }

    private SeoAuthor getAuthor(Paste paste) {
        if (paste.getUserId() == null) return null;

        User user = paste.getUser();
        if (user == null || user.getUniqueName() == null || user.getUniqueName().trim().isEmpty()) return null;

        String username = user.getUniqueName().trim();
        String displayName = user.getName() == null || user.getName().trim().isEmpty()
                ? username
                : user.getName().trim();
        return new SeoAuthor(displayName, username, seo.absoluteUrl("/@" + seo.pathSegment(username)));
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

        String preview = content == null ? "" : seo.truncateWithoutEllipsis(content, previewLength);
        String authorHtml = author == null
                ? ""
                : "<p>By <a href=\"" + seo.escapeHtml(author.profileUrl) + "\">"
                + seo.escapeHtml(author.displayName) + " (@" + seo.escapeHtml(author.username) + ")</a></p>";

        String tagsHtml = "";

        for (String t : paste.getTags()) {
            String tag = t.trim();
            if (!tag.isEmpty()) {
                String tagUrl = seo.absoluteUrl("/tags/" + seo.pathSegment(tag));
                tagsHtml += "<a href=\"" + seo.escapeHtml(tagUrl) + "\" class=\"seo-tag\">" + seo.escapeHtml(tag) + "</a> ";
            }
        }

        return "<main id=\"seo-content\">"
                + "<h1 title=\"paste-title\">" + seo.escapeHtml(title) + "</h1>"
                + authorHtml
                + "<p>View and share code snippets on Pastefy.</p>"
                + "<pre><code>" + seo.escapeHtml(preview) + "</code></pre>"
                + tagsHtml
                + "</main>";
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
