package de.interaapps.pastefy.model.database.algorithm;

import de.interaapps.pastefy.Pastefy;
import de.interaapps.pastefy.ai.PasteAI;
import de.interaapps.pastefy.model.database.PasteTag;
import org.javawebstack.orm.Model;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Filterable;
import org.javawebstack.orm.annotation.Searchable;
import org.javawebstack.orm.annotation.Table;


@Table("tag_listing")
public class TagListing extends Model {
    @Column(id = true, size = 30)
    @Filterable
    @Searchable
    public String tag;

    @Column
    public String displayName;

    @Column
    public String imageUrl;

    @Column
    public String description;

    @Column
    public String website;

    @Column
    public String icon;

    @Column
    public int pasteCount = 0;


    public static TagListing getOrCreate(String tag) {
        if (tag.length() > 30)
            tag = tag.substring(0, 30);

        TagListing tagListing = Repo.get(TagListing.class).get(tag);
        if (tagListing == null) {
            tagListing = new TagListing();
            tagListing.tag = tag;
            tagListing.save();

            if (Pastefy.getInstance().aiEnabled()) {
                try {
                    final TagListing finalTagListing = tagListing;
                    new Thread(() -> {
                        finalTagListing.description = Pastefy.getInstance().getPasteAI().generateTagDescription(finalTagListing);
                        finalTagListing.save();
                    }).start();
                } catch (Exception ignored) {}
            }
        }
        return tagListing;
    }

    public static void updateCount(String tag) {
        TagListing tagListing = getOrCreate(tag);
        tagListing.pasteCount = Repo.get(PasteTag.class).where("tag", tag).count();
        tagListing.save();
    }
}
