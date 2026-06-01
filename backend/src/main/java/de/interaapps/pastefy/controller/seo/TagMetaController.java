package de.interaapps.pastefy.controller.seo;

import de.interaapps.pastefy.Pastefy;
import de.interaapps.pastefy.controller.HttpController;
import de.interaapps.pastefy.model.database.algorithm.TagListing;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.router.annotation.PathPrefix;
import org.javawebstack.http.router.router.annotation.params.Path;
import org.javawebstack.http.router.router.annotation.verbs.Get;

@PathPrefix("/tags")
public class TagMetaController extends HttpController {
    private static final int DESCRIPTION_MAX_LENGTH = 180;

    private final SeoRenderer seo = new SeoRenderer();

    @Get("/{tag}")
    public String tagMeta(Exchange exchange, @Path("tag") String tag) {
        return seo.render(exchange, () -> {
            if (!Pastefy.getInstance().publicPastesEnabled() || tag == null || tag.trim().isEmpty()) return null;

            TagListing tagListing = TagListing.getOrCreate(tag);
            String tagName = getTagName(tagListing);
            String title = tagName + " | Pastefy";
            String description = getDescription(tagListing, tagName);
            String path = "/tags/" + seo.pathSegment(tagListing.tag);

            SeoRenderer.SeoPage page = seo.page(path, title, description)
                    .content(getSeoContent(tagListing, tagName));

            if (tagListing.imageUrl != null && !tagListing.imageUrl.trim().isEmpty()) {
                page.image(tagListing.imageUrl);
            }

            return page;
        });
    }

    private String getTagName(TagListing tagListing) {
        String name = tagListing.displayName == null || tagListing.displayName.trim().isEmpty()
                ? tagListing.tag
                : tagListing.displayName;
        return seo.truncate(name.trim().replaceAll("\\s+", " "), 120);
    }

    private String getDescription(TagListing tagListing, String tagName) {
        if (tagListing.description != null && !tagListing.description.trim().isEmpty()) {
            return seo.truncate(tagListing.description.trim().replaceAll("\\s+", " "), DESCRIPTION_MAX_LENGTH);
        }
        return seo.truncate("Explore public pastes tagged \"" + tagName + "\" on Pastefy.", DESCRIPTION_MAX_LENGTH);
    }

    private String getSeoContent(TagListing tagListing, String tagName) {
        return "<main id=\"seo-content\">"
                + "<h1>" + seo.escapeHtml(tagName) + "</h1>"
                + "<p>" + seo.escapeHtml(getDescription(tagListing, tagName)) + "</p>"
                + "<p>Public pastes: " + tagListing.pasteCount + "</p>"
                + "</main>";
    }
}
