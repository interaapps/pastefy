package de.interaapps.pastefy;

import de.interaapps.accounts.apiclient.AccountsClient;
import de.interaapps.pastefy.auth.AuthMiddleware;
import de.interaapps.pastefy.auth.OAuth2Callback;
import de.interaapps.pastefy.controller.PasteController;
import de.interaapps.pastefy.model.database.AuthKey;
import de.interaapps.pastefy.model.database.Paste;
import de.interaapps.pastefy.model.database.User;
import de.interaapps.pastefy.model.responses.ExceptionResponse;
import org.javawebstack.framework.HttpController;
import org.javawebstack.framework.WebApplication;
import org.javawebstack.framework.config.Config;
import org.javawebstack.httpclient.HTTPClient;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.HTTPServer;
import org.javawebstack.httpserver.handler.Middleware;
import org.javawebstack.httpserver.handler.RequestHandler;
import org.javawebstack.orm.ORM;
import org.javawebstack.orm.ORMConfig;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.exception.ORMConfigurationException;
import org.javawebstack.orm.mapper.AbstractDataTypeMapper;
import org.javawebstack.orm.wrapper.SQL;
import org.javawebstack.passport.OAuth2Module;
import org.javawebstack.passport.Profile;
import org.javawebstack.passport.services.oauth2.*;
import org.javawebstack.webutils.middlewares.RateLimitMiddleware;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Pastefy extends WebApplication {

    private static Pastefy instance;
    private OAuth2Module oAuth2Module;

    public static Pastefy getInstance() {
        return instance;
    }

    protected void setupConfig(Config config) {
        Map<String, String> map = new HashMap<>();
        map.put("INTERAAPPS_AUTH_KEY", "interaapps.auth.key");
        map.put("INTERAAPPS_AUTH_ID", "interaapps.auth.id");
        map.put("AUTH_PROVIDER", "auth.provider");

        map.put("SERVER_NAME", "server.name");

        map.put("RATELIMITER_MILLIS", "ratelimiter.millis");
        map.put("RATELIMITER_LIMIT", "ratelimiter.limit");

        map.put("OAUTH2_INTERAAPPS_CLIENT_ID", "oauth2.interaapps.id");
        map.put("OAUTH2_INTERAAPPS_CLIENT_SECRET", "oauth2.interaapps.secret");
        map.put("OAUTH2_TWITCH_CLIENT_ID", "oauth2.twitch.id");
        map.put("OAUTH2_TWITCH_CLIENT_SECRET", "oauth2.twitch.secret");
        map.put("OAUTH2_GITHUB_CLIENT_ID", "oauth2.github.id");
        map.put("OAUTH2_GITHUB_CLIENT_SECRET", "oauth2.github.secret");
        map.put("OAUTH2_GOOGLE_CLIENT_ID", "oauth2.google.id");
        map.put("OAUTH2_GOOGLE_CLIENT_SECRET", "oauth2.google.secret");
        map.put("OAUTH2_DISCORD_CLIENT_ID", "oauth2.discord.id");
        map.put("OAUTH2_DISCORD_CLIENT_SECRET", "oauth2.discord.secret");

        config.addEnvKeyMapping(map);
        config.addEnvFile(new File(".env"));

        if (!getConfig().get("oauth2.interaapps.id", "NONE").equalsIgnoreCase("NONE"))
            oAuth2Module.addService(new InteraAppsOAuth2Service(getConfig().get("oauth2.interaapps.id"), getConfig().get("oauth2.interaapps.secret"), getConfig().get("server.name", "http://localhost:1337")).setScopes(new String[]{"user:read", "contacts.accepted:read"}));
        if (!getConfig().get("oauth2.google.id", "NONE").equalsIgnoreCase("NONE"))
            oAuth2Module.addService(new GoogleOAuth2Service(getConfig().get("oauth2.google.id"), getConfig().get("oauth2.google.secret"), getConfig().get("server.name", "http://localhost:1337")));
        if (!getConfig().get("oauth2.discord.id", "NONE").equalsIgnoreCase("NONE"))
            oAuth2Module.addService(new DiscordOAuth2Service(getConfig().get("oauth2.discord.id"), getConfig().get("oauth2.discord.secret"), getConfig().get("server.name", "http://localhost:1337")));
        if (!getConfig().get("oauth2.github.id", "NONE").equalsIgnoreCase("NONE"))
            oAuth2Module.addService(new GithubOAuth2Service(getConfig().get("oauth2.github.id"), getConfig().get("oauth2.github.secret"), getConfig().get("server.name", "http://localhost:1337")));
        if (!getConfig().get("oauth2.twitch.id", "NONE").equalsIgnoreCase("NONE"))
            oAuth2Module.addService(new TwitchOAuth2Service(getConfig().get("oauth2.twitch.id"), getConfig().get("oauth2.twitch.secret"), getConfig().get("server.name", "http://localhost:1337"), oAuth2Module));
    }

    protected void setupModels(SQL sql) throws ORMConfigurationException {
        Handler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        Logger.getLogger("ORM").addHandler(handler);
        Logger.getLogger("ORM").setLevel(Level.ALL);
        ORMConfig config = new ORMConfig()
            .setTablePrefix("pastefy_")
            .addTypeMapper(new AbstractDataTypeMapper());
        ORM.register(Paste.class.getPackage(), sql, config);
        ORM.autoMigrate();
    }

    protected void setupServer(HTTPServer server) {
        oAuth2Module.getServices().forEach(authService -> {
            System.out.println("ADDED "+authService.getName()+" ON "+oAuth2Module.getPathPrefix()+""+ authService.getName());
        });

        oAuth2Module.setOAuth2Callback(new OAuth2Callback());

        server.exceptionHandler((exchange, throwable) -> new ExceptionResponse(throwable));
        server.middleware("auth", new AuthMiddleware());

        if (getConfig().has("ratelimiter.millis"))
            server.middleware("rate-limiter", new RateLimitMiddleware(getConfig().getInt("ratelimiter.millis", 5000), getConfig().getInt("ratelimiter.limit", 5)).createAutoDeadRateLimitsRemover(1000*60*10));
        //else
        //    server.middleware("rate-limiter", e->null);

        server.beforeInterceptor(exchange -> {
            exchange.header("Server", "InteraApps-Pastefy");

            exchange.attrib("loggedIn", false);
            exchange.attrib("user", null);
            String accessToken = null;
            if (exchange.header("x-auth-key") != null)
                accessToken = exchange.header("x-auth-key");
            if (exchange.bearerAuth() != null)
                accessToken = exchange.bearerAuth();

            if (accessToken != null) {
                AuthKey authKey = Repo.get(AuthKey.class).where("key", accessToken).first();
                if (oAuth2Module.getServices().size() > 0 && authKey != null) {
                    User user = Repo.get(User.class).get(authKey.userId);

                    if (user != null) {
                        exchange.attrib("loggedIn", true);
                        exchange.attrib("user", user);
                        exchange.attrib("authkey", authKey);
                    }
                }
            }

            return false;
        });

        server.controller(HttpController.class, PasteController.class.getPackage());


        RequestHandler requestHandler = exchange -> {
            try {
                exchange.write(getClass().getClassLoader().getResourceAsStream("static/index.html"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        };
        server.get("/", requestHandler);
        server.staticResourceDirectory("/", "static");
        server.get("/{*:path}", requestHandler);
    }

    protected void setupModules() {
        oAuth2Module = new OAuth2Module();
        oAuth2Module.setPathPrefix("/api/v2/auth/oauth2/");
        addModule(oAuth2Module);
    }

    public static void main(String[] args) {
        instance = new Pastefy();
        instance.run(args);
    }

    public OAuth2Module getoAuth2Module() {
        return oAuth2Module;
    }
}
