package de.interaapps.pastefy;

import de.interaapps.pastefy.auth.*;
import de.interaapps.pastefy.controller.HttpController;
import de.interaapps.pastefy.controller.PasteController;
import de.interaapps.pastefy.exceptions.AuthenticationException;
import de.interaapps.pastefy.exceptions.FeatureDisabledException;
import de.interaapps.pastefy.exceptions.NotFoundException;
import de.interaapps.pastefy.exceptions.PermissionsDeniedException;
import de.interaapps.pastefy.model.database.AuthKey;
import de.interaapps.pastefy.model.database.Paste;
import de.interaapps.pastefy.model.database.User;
import de.interaapps.pastefy.model.responses.ExceptionResponse;
import org.eclipse.jetty.util.IO;
import org.javawebstack.httpserver.HTTPServer;
import org.javawebstack.httpserver.handler.RequestHandler;
import org.javawebstack.httpserver.handler.StaticFileHandler;
import org.javawebstack.orm.ORM;
import org.javawebstack.orm.ORMConfig;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.exception.ORMConfigurationException;
import org.javawebstack.orm.mapper.AbstractDataTypeMapper;
import org.javawebstack.orm.wrapper.SQLDriverFactory;
import org.javawebstack.orm.wrapper.SQLDriverNotFoundException;
import org.javawebstack.passport.Passport;
import org.javawebstack.passport.strategies.oauth2.OAuth2Strategy;
import org.javawebstack.passport.strategies.oauth2.providers.*;
import org.javawebstack.webutils.config.Config;
import org.javawebstack.webutils.config.EnvFile;
import org.javawebstack.webutils.middleware.CORSPolicy;
import org.javawebstack.webutils.middleware.MultipartPolicy;
import org.javawebstack.webutils.middleware.SerializedResponseTransformer;
import org.javawebstack.webutils.middlewares.RateLimitMiddleware;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Pastefy {

    private static Pastefy instance;

    public static Pastefy getInstance() {
        return instance;
    }

    private Config config;
    private Passport passport;
    private OAuth2Strategy oAuth2Strategy;
    private HTTPServer httpServer;
    private boolean loginRequired = false;
    private boolean loginRequiredForRead = false;
    private boolean loginRequiredForCreate = false;
    private boolean publicPastesEnabled = false;

    public Pastefy() {
        config = new Config();
        httpServer = new HTTPServer();
        passport = new Passport("/api/v2/auth");

        setupConfig();
        oAuth2Strategy = new OAuth2Strategy(getConfig().get("server.name", "http://localhost"));
        oAuth2Strategy.setHttpCallbackHandler(new OAuth2Callback());

        if (!getConfig().get("oauth2.interaapps.id", "NONE").equalsIgnoreCase("NONE"))
            oAuth2Strategy.use("interaapps", new InteraAppsOAuth2Provider(getConfig().get("oauth2.interaapps.id"), getConfig().get("oauth2.interaapps.secret")).setScopes("user:read", "contacts.accepted:read"));
        if (!getConfig().get("oauth2.google.id", "NONE").equalsIgnoreCase("NONE"))
            oAuth2Strategy.use("google", new GoogleOAuth2Provider(getConfig().get("oauth2.google.id"), getConfig().get("oauth2.google.secret")));
        if (!getConfig().get("oauth2.discord.id", "NONE").equalsIgnoreCase("NONE"))
            oAuth2Strategy.use("discord", new DiscordOAuth2Provider(getConfig().get("oauth2.discord.id"), getConfig().get("oauth2.discord.secret")));
        if (!getConfig().get("oauth2.github.id", "NONE").equalsIgnoreCase("NONE"))
            oAuth2Strategy.use("github", new GitHubOAuth2Provider(getConfig().get("oauth2.github.id"), getConfig().get("oauth2.github.secret")));
        if (!getConfig().get("oauth2.twitch.id", "NONE").equalsIgnoreCase("NONE"))
            oAuth2Strategy.use("twitch", new TwitchOAuth2Provider(getConfig().get("oauth2.twitch.id"), getConfig().get("oauth2.twitch.secret")));

        passport.use("oauth2", oAuth2Strategy);

        setupModels();
        setupServer();
    }

    protected void setupConfig() {
        Map<String, String> map = new HashMap<>();
        map.put("HTTP_SERVER_PORT", "http.server.port");
        map.put("HTTP_SERVER_CORS", "http.server.cors");

        map.put("DATABASE_DRIVER", "database.driver");
        map.put("DATABASE_NAME", "database.name");
        map.put("DATABASE_USER", "database.user");
        map.put("DATABASE_PASSWORD", "database.password");
        map.put("DATABASE_HOST", "database.host");
        map.put("DATABASE_PORT", "database.port");

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

        map.put("PASTEFY_INFO_CUSTOM_LOGO", "pastefy.info.custom.logo");
        map.put("PASTEFY_INFO_CUSTOM_NAME", "pastefy.info.custom.name");
        map.put("PASTEFY_INFO_CUSTOM_FOOTER", "pastefy.info.custom.footer");

        map.put("PASTEFY_ENCRYPTION_DEFAULT", "pastefy.encryption.default");

        map.put("PASTEFY_GRANT_ACCESS_REQUIRED", "pastefy.grantaccessrequired");

        map.put("PASTEFY_LOGIN_REQUIRED", "pastefy.loginrequired");
        map.put("PASTEFY_LOGIN_REQUIRED_CREATE", "pastefy.loginrequired.create");
        map.put("PASTEFY_LOGIN_REQUIRED_READ", "pastefy.loginrequired.read");

        map.put("PASTEFY_LIST_PASTES", "pastefy.listpastes");
        map.put("PASTEFY_PUBLIC_STATS", "pastefy.publicstats");

        map.put("PASTEFY_PUBLIC_PASTES", "pastefy.publicpastes");

        File file = new File(".env");
        if (file.exists()) {
            config.add(new EnvFile(file).withVariables(), map);
        } else {
            config.add(new EnvFile().withVariables(), map);
        }

        loginRequired = config.get("pastefy.loginrequired", "false").toLowerCase(Locale.ROOT).equals("true");
        loginRequiredForCreate = loginRequired || config.get("pastefy.loginrequired.create", "false").toLowerCase(Locale.ROOT).equals("true");
        loginRequiredForRead = loginRequired || config.get("pastefy.loginrequired.read", "false").toLowerCase(Locale.ROOT).equals("true");
        publicPastesEnabled = config.get("pastefy.publicpastes", "true").toLowerCase(Locale.ROOT).equals("true");
    }

    protected void setupModels() {
        try {
            SQLDriverFactory sqlDriverFactory = new SQLDriverFactory(new HashMap<String, String>() {{
                put("file", config.get("database.file", "sb.sqlite"));
                put("host", config.get("database.host", "localhost"));
                put("port", config.get("database.port", "3306"));
                put("name", config.get("database.name", "app"));
                put("user", config.get("database.user", "root"));
                put("password", config.get("database.password", ""));
            }});

            Handler handler = new ConsoleHandler();
            handler.setLevel(Level.ALL);
            Logger.getLogger("ORM").addHandler(handler);
            Logger.getLogger("ORM").setLevel(Level.ALL);
            ORMConfig ormConfig = new ORMConfig()
                    .setTablePrefix("pastefy_")
                    .addTypeMapper(new AbstractDataTypeMapper());

            ORM.register(Paste.class.getPackage(), sqlDriverFactory.getDriver(config.get("database.driver", "none")), ormConfig);
            ORM.autoMigrate();
        } catch (SQLDriverNotFoundException | ORMConfigurationException e) {
            e.printStackTrace();
        }
    }

    protected void setupServer() {
        httpServer.port(config.getInt("http.server.port", 80));

        if (config.has("http.server.cors")) {
            httpServer.beforeInterceptor(new CORSPolicy(config.get("http.server.cors")));
        }

        httpServer.beforeInterceptor(new MultipartPolicy(config.get("http.server.tmp", null)));
        httpServer.responseTransformer(new SerializedResponseTransformer().ignoreStrings());

        oAuth2Strategy.getProviders().forEach((name, authService) -> {
            System.out.println("ADDED " + name + " as auth provider");
        });

        httpServer.exceptionHandler((exchange, throwable) -> {
            if (throwable instanceof RuntimeException) {
                if (throwable.getCause() != null)
                    throwable = throwable.getCause();
            }

            if (throwable instanceof AuthenticationException) {
                exchange.status(401);
            } else if (throwable instanceof NotFoundException) {
                exchange.status(404);
            } else if (throwable instanceof PermissionsDeniedException) {
                exchange.status(403);
            } else {
                exchange.status(500);
            }

            return new ExceptionResponse(throwable);
        });
        httpServer.middleware("auth", new AuthMiddleware());
        httpServer.middleware("admin", new AdminMiddleware());


        httpServer.middleware("blocked-check", new BlockedMiddleware());
        httpServer.middleware("awaiting-access-check", new AwaitingAccessMiddleware());

        httpServer.middleware("auth-login-required-read", exchange -> {
            if (loginRequiredForRead && (exchange.attrib("user") == null || !((User) exchange.attrib("user")).roleCheck()))
                throw new AuthenticationException();
            return null;
        });
        httpServer.middleware("auth-login-required-create", exchange -> {
            if (loginRequiredForCreate && (exchange.attrib("user") == null || !((User) exchange.attrib("user")).roleCheck()))
                throw new AuthenticationException();
            return null;
        });

        httpServer.middleware("public-pastes-endpoint", exchange -> {
            if (!publicPastesEnabled)
                throw new FeatureDisabledException();
            return null;
        });

        if (getConfig().has("ratelimiter.millis"))
            httpServer.middleware("rate-limiter", new RateLimitMiddleware(getConfig().getInt("ratelimiter.millis", 5000), getConfig().getInt("ratelimiter.limit", 5)).createAutoDeadRateLimitsRemover(1000 * 60 * 10));
        else
            httpServer.middleware("rate-limiter", e -> null);

        httpServer.beforeInterceptor(exchange -> {
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
                if (oAuth2Strategy.getProviders().size() > 0 && authKey != null) {
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


        RequestHandler requestHandler = exchange -> {
            try {
                InputStream file = getClass().getClassLoader().getResourceAsStream("static/index.html");
                if (file == null) {
                    throw new NotFoundException();
                }

                exchange.header("Content-Type", "text/html; charset=UTF-8");
                exchange.write(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        };

        httpServer.controller(HttpController.class, PasteController.class.getPackage());

        passport.createRoutes(httpServer);

        httpServer.get("/", requestHandler);
        httpServer.staticResourceDirectory("/", "static");

        httpServer.get("/api/{*:path}", e -> {
            throw new NotFoundException();
        });

        httpServer.get("/{*:path}", requestHandler);

        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            public void run() {
                // Delete expired pastes
                Repo.get(Paste.class)
                        .where("expireAt", "<", Timestamp.from(Instant.now()))
                        .notNull("expireAt")
                        .delete();
            }
        }, 0, 1000 * 60);
    }

    public void start() {
        httpServer.start();
    }

    public static void main(String[] args) {
        instance = new Pastefy();
        instance.start();
    }

    public Passport getPassport() {
        return passport;
    }

    public OAuth2Strategy getOAuth2Strategy() {
        return oAuth2Strategy;
    }

    public Config getConfig() {
        return config;
    }

    public boolean isLoginRequired() {
        return loginRequired;
    }

    public boolean isLoginRequiredForCreate() {
        return loginRequiredForCreate;
    }

    public boolean isLoginRequiredForRead() {
        return loginRequiredForRead;
    }


    public boolean publicPastesEnabled() {
        return publicPastesEnabled;
    }
}
