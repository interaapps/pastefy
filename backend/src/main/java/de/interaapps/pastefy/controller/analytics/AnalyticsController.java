package de.interaapps.pastefy.controller.analytics;

import de.interaapps.pastefy.Pastefy;
import de.interaapps.pastefy.analytics.AnalyticsQuery;
import de.interaapps.pastefy.analytics.AnalyticsResponse;
import de.interaapps.pastefy.controller.HttpController;
import de.interaapps.pastefy.exceptions.FeatureDisabledException;
import de.interaapps.pastefy.exceptions.PermissionsDeniedException;
import de.interaapps.pastefy.model.database.Paste;
import de.interaapps.pastefy.model.database.AuthKey;
import de.interaapps.pastefy.model.database.User;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.router.annotation.PathPrefix;
import org.javawebstack.http.router.router.annotation.With;
import org.javawebstack.http.router.router.annotation.params.Attrib;
import org.javawebstack.http.router.router.annotation.params.Path;
import org.javawebstack.http.router.router.annotation.verbs.Get;

import java.util.Objects;

@PathPrefix("/api/v2/analytics")
@With("auth")
public class AnalyticsController extends HttpController {
    @Get("/admin")
    @With("admin")
    public AnalyticsResponse admin(Exchange exchange, @Attrib("authkey") AuthKey authKey) {
        authKey.checkPermission("analytics:read");
        return query(exchange);
    }

    @Get("/pastes/{id}")
    public AnalyticsResponse paste(Exchange exchange, @Path("id") String id, @Attrib("user") User user, @Attrib("authkey") AuthKey authKey) {
        authKey.checkPermission("analytics:read");
        Paste paste = Paste.get(id);
        if (paste == null || (!user.isAdmin() && !Objects.equals(user.getId(), paste.getUserId()))) {
            throw new PermissionsDeniedException();
        }
        AnalyticsQuery query = AnalyticsQuery.from(exchange);
        query.filters.put("paste_key", paste.getKey());
        return query(query);
    }

    @Get("/user")
    public AnalyticsResponse user(Exchange exchange, @Attrib("user") User user, @Attrib("authkey") AuthKey authKey) {
        authKey.checkPermission("analytics:read");
        AnalyticsQuery query = AnalyticsQuery.from(exchange);
        query.filters.put("paste_user_id", user.getId());
        return query(query);
    }

    private AnalyticsResponse query(Exchange exchange) {
        return query(AnalyticsQuery.from(exchange));
    }

    private AnalyticsResponse query(AnalyticsQuery query) {
        if (!Pastefy.getInstance().analyticsEnabled()) throw new FeatureDisabledException();
        return Pastefy.getInstance().getAnalyticsService().query(query);
    }
}
