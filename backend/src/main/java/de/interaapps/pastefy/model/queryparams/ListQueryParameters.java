package de.interaapps.pastefy.model.queryparams;

import de.interaapps.pastefy.Pastefy;
import de.interaapps.pastefy.helper.AbstractDataHelper;
import org.javawebstack.abstractdata.AbstractElement;
import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.http.router.Exchange;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

public class ListQueryParameters {
    public Integer page = 1;
    public Integer pageLimit;
    public String search;
    public AbstractObject filters = new AbstractObject();
    public String sort = null;

    public ListQueryParameters(Exchange exchange, Consumer<ListQueryParameters> function) {
        function.accept(this);
        AbstractObject params = exchange.getQueryParameters();
        int configLimit = Pastefy.getInstance().getConfig().getInt("pastefy.paginaton.pagelimit", 50);

        if (exchange.query("page") != null)
            page = Integer.parseInt(exchange.query("page"));

        pageLimit = configLimit;
        if (exchange.query("page_limit") != null)
            pageLimit = exchange.query("page_limit", Integer.class);

        if (exchange.query("search") != null)
            search = exchange.query("search");

        if (Pastefy.getInstance().getConfig().has("pastefy.paginaton.pagelimit")) {
            if (pageLimit > configLimit)
                pageLimit = configLimit;
        }

        if (exchange.query("sort") != null)
            sort = exchange.query("sort");

        if (params.has("filters")) {
            filters = AbstractElement.fromJson(params.get("filters").string()).object();
        } else if (params.has("filter")) {
            filters = params.get("filter").object();
            filters.forEach((key, value) -> filters.set(key, AbstractDataHelper.ifArrayConvertToArray(value)));
            filters = new AbstractObject().set("$and", filters);
        }
    }

    public ListQueryParameters setFilter(String key, AbstractElement value) {
        filters.set(key, value);
        return this;
    }
    public ListQueryParameters setFilter(String key, String value) {
        filters.set(key, value);
        return this;
    }
    public <T extends Enum> ListQueryParameters setFilter(String key, T value) {
        return setFilter(key, value.name());
    }

    public ListQueryParameters setSearch(String search) {
        this.search = search;
        return this;
    }

    public ListQueryParameters setPage(int page) {
        this.page = page;
        return this;
    }

    public ListQueryParameters setPageLimit(int pageLimit) {
        this.pageLimit = pageLimit;
        return this;
    }

    public ListQueryParameters setSort(String sort) {
        this.sort = sort;
        return this;
    }

    public ListQueryParameters guarded(Exchange exchange) {
        return this;
    }

    public static String fromDate(long time) {
        if (Pastefy.getInstance().isElasticsearchEnabled()) {
            return String.valueOf(time);
        }
        Date date = new Date();
        date.setTime(time);
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }
}
