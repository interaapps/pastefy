package de.interaapps.pastefy.helper;

import de.interaapps.pastefy.Pastefy;
import de.interaapps.pastefy.exceptions.PermissionsDeniedException;
import de.interaapps.pastefy.model.database.Paste;
import de.interaapps.pastefy.model.database.PasteTag;
import de.interaapps.pastefy.model.database.User;
import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.orm.query.Query;

public class RequestHelper {

    public static void pagination(Query<?> query, Exchange exchange) {
        int page = 0;
        if (exchange.query("page") != null)
            page = Integer.parseInt(exchange.query("page")) - 1;

        int limit = 10;

        if (exchange.query("page_limit") != null)
            limit = exchange.query("page_limit", Integer.class);

        if (Pastefy.getInstance().getConfig().has("pastefy.paginaton.pagelimit")) {
            int configLimit = Pastefy.getInstance().getConfig().getInt("pastefy.paginaton.pagelimit", 50);
            if (limit > configLimit)
                limit = configLimit;
        }

        query.limit(limit).offset(page * limit);

        exchange.header("PAGINATION_LIMIT", String.valueOf(limit));
        exchange.header("PAGINATION_PAGE", String.valueOf(page));
    }

    public static void queryFilter(Query<?> query, AbstractObject params) {
        if (params.has("filter")) {
            query.filter(params.get("filter").toFormData().getMap());
        }
    }

    public static void filterTags(Query<Paste> query, AbstractObject params) {
        if (params.has("filter_tags")) {
            String[] filterTags = params.get("filter_tags").string().split(",");

            for (String filterTag : filterTags) {
                query.whereExists(PasteTag.class, (pasteTagQuery) -> pasteTagQuery.where("tag", filterTag).where(PasteTag.class, "paste", "=", Paste.class, "key"));
            }
        }
    }

    public static void userIdPastesFilter(User user, Query<?> query, Exchange exchange) {
        boolean isAdmin = user != null && user.isAdmin();
        String filterUserId = null;

        if (user == null && !Pastefy.getInstance().getConfig().get("pastefy.listpastes", "false").equalsIgnoreCase("true"))
            throw new PermissionsDeniedException();

        if (user != null && !isAdmin)
            filterUserId = user.id;

        if (isAdmin && exchange.query("user_id") != null)
            filterUserId = exchange.query("user_id");

        if (filterUserId != null)
            query.where("userId", filterUserId);
    }
}
