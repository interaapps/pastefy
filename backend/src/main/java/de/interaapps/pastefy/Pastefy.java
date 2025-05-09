package de.interaapps.pastefy;

import com.google.gson.Gson;
import de.interaapps.pastefy.ai.PasteAI;
import de.interaapps.pastefy.auth.*;
import de.interaapps.pastefy.auth.strategies.oauth2.OAuth2Strategy;
import de.interaapps.pastefy.auth.strategies.oauth2.providers.*;
import de.interaapps.pastefy.controller.AppController;
import de.interaapps.pastefy.controller.HttpController;
import de.interaapps.pastefy.exceptions.AuthenticationException;
import de.interaapps.pastefy.exceptions.FeatureDisabledException;
import de.interaapps.pastefy.exceptions.NotFoundException;
import de.interaapps.pastefy.exceptions.PermissionsDeniedException;
import de.interaapps.pastefy.model.database.AuthKey;
import de.interaapps.pastefy.model.database.Paste;
import de.interaapps.pastefy.model.database.User;
import de.interaapps.pastefy.model.plugins.PastefyBackendPlugin;
import de.interaapps.pastefy.model.plugins.PastefyPlugin;
import de.interaapps.pastefy.model.responses.ExceptionResponse;
import io.undertow.util.FileUtils;
import org.javawebstack.http.router.HTTPRouter;
import org.javawebstack.http.router.handler.RequestHandler;
import org.javawebstack.http.router.transformer.response.SerializedResponseTransformer;
import org.javawebstack.http.router.undertow.UndertowHTTPSocketServer;
import org.javawebstack.orm.ORM;
import org.javawebstack.orm.ORMConfig;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.connection.MySQL;
import org.javawebstack.orm.connection.SQL;
import org.javawebstack.orm.connection.SQLite;
import org.javawebstack.orm.connection.pool.MinMaxScaler;
import org.javawebstack.orm.connection.pool.SQLPool;
import org.javawebstack.orm.exception.ORMConfigurationException;
import org.javawebstack.orm.mapper.AbstractDataTypeMapper;
import org.javawebstack.webutils.config.Config;
import org.javawebstack.webutils.config.EnvFile;
import org.javawebstack.webutils.config.Mapping;
import org.javawebstack.webutils.config.MappingTryout;
import org.javawebstack.webutils.middleware.CORSPolicy;
import org.javawebstack.webutils.middleware.MultipartPolicy;
import org.javawebstack.webutils.middleware.RateLimitMiddleware;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Pastefy {

    private static Pastefy instance;

    public static Pastefy getInstance() {
        return instance;
    }

    private final Config config;
    private Passport passport;
    private OAuth2Strategy oAuth2Strategy;
    private final HTTPRouter httpRouter;
    private boolean loginRequired = false;
    private boolean loginRequiredForRead = false;
    private boolean loginRequiredForCreate = false;
    private boolean publicPastesEnabled = false;

    private PasteAI pasteAI;

    private String homeHTML;

    private final Map<String, PastefyPlugin> plugins = new HashMap<>();

    public Pastefy() {
        config = new Config();
        httpRouter = new HTTPRouter(new UndertowHTTPSocketServer());

        initPlugins();

        setupConfig();
        setupPassport();
        setupModels();
        setupServer();

        if (config.has("ai.antrophic.token")) {
            pasteAI = new PasteAI(this);
        }
    }

    protected void setupConfig() {
        Mapping mapping = new Mapping()
                .map("OAUTH2_INTERAAPPS_CLIENT_ID", "oauth2.interaapps.id")
                .map("OAUTH2_INTERAAPPS_CLIENT_SECRET", "oauth2.interaapps.secret")
                .map("OAUTH2_TWITCH_CLIENT_ID", "oauth2.twitch.id")
                .map("OAUTH2_TWITCH_CLIENT_SECRET", "oauth2.twitch.secret")
                .map("OAUTH2_GITHUB_CLIENT_ID", "oauth2.github.id")
                .map("OAUTH2_GITHUB_CLIENT_SECRET", "oauth2.github.secret")
                .map("OAUTH2_GOOGLE_CLIENT_ID", "oauth2.google.id")
                .map("OAUTH2_GOOGLE_CLIENT_SECRET", "oauth2.google.secret")
                .map("OAUTH2_DISCORD_CLIENT_ID", "oauth2.discord.id")
                .map("OAUTH2_DISCORD_CLIENT_SECRET", "oauth2.discord.secret")
                .map("PASTEFY_GRANT_ACCESS_REQUIRED", "pastefy.grantaccessrequired")
                .map("PASTEFY_LOGIN_REQUIRED", "pastefy.loginrequired")
                .map("PASTEFY_LOGIN_REQUIRED_CREATE", "pastefy.loginrequired.create")
                .map("PASTEFY_LOGIN_REQUIRED_READ", "pastefy.loginrequired.read")
                .map("PASTEFY_LIST_PASTES", "pastefy.listpastes")
                .map("PASTEFY_PUBLIC_STATS", "pastefy.publicstats")
                .map("PASTEFY_PUBLIC_PASTES", "pastefy.publicpastes")
                .map("PASTEFY_META_TAGS", "pastefy.metatags")
                .map("PASTEFY_CUSTOM_BODY", "pastefy.custombody")
                .map("PASTEFY_CUSTOM_HEADER", "pastefy.customheader")
                .map("PASTEFY_PAGINATION_PAGE_LIMIT", "pastefy.paginaton.pagelimit")

                .map("AI_ANTHROPIC_TOKEN", "ai.antrophic.token")

                .map("DATABASE_CUSTOMPARAMS_CACHE_PREP_STMTS", "database.customparams.cachePrepStmts")
                .map("DATABASE_CUSTOMPARAMS_PREP_STMT_CACHE_SIZE", "database.customparams.prepStmtCacheSize")
                .map("DATABASE_CUSTOMPARAMS_PREP_STMT_CACHE_SQL_LIMIT", "database.customparams.prepStmtCacheSqlLimit")
                .map("DATABASE_CUSTOMPARAMS_USE_SERVER_PREP_STMTS", "database.customparams.useServerPrepStmts")
                .map("DATABASE_CUSTOMPARAMS_CACHE_RESULT_SET_METADATA", "database.customparams.cacheResultSetMetadata")
                .map("DATABASE_CUSTOMPARAMS_CACHE_SERVER_CONFIGURATION", "database.customparams.cacheServerConfiguration")
                .map("DATABASE_CUSTOMPARAMS_MAINTAIN_TIME_STATS", "database.customparams.maintainTimeStats");

        getBackendPlugins().forEach(p -> p.customConfigMapping(mapping));

        config.add(new EnvFile(new File(".env")).withVariables(), new MappingTryout(mapping, Config::basicEnvMapping));

        loginRequired = config.get("pastefy.loginrequired", "false").toLowerCase(Locale.ROOT).equals("true");
        loginRequiredForCreate = loginRequired || config.get("pastefy.loginrequired.create", "false").toLowerCase(Locale.ROOT).equals("true");
        loginRequiredForRead = loginRequired || config.get("pastefy.loginrequired.read", "false").toLowerCase(Locale.ROOT).equals("true");
        publicPastesEnabled = config.get("pastefy.publicpastes", "true").toLowerCase(Locale.ROOT).equals("true");
    }

    protected void initPlugins() {
        // Go through plugins folder and register everything
        File pluginsFolder = new File("plugins");
        if (!pluginsFolder.exists() || !pluginsFolder.isDirectory())
            return;

        for (File file : pluginsFolder.listFiles()) {
            File pluginFolder = new File(file.getPath());
            if (!pluginsFolder.exists() || !pluginsFolder.isDirectory())
                continue;

            try {
                PastefyPlugin pastefyPlugin = new PastefyPlugin(pluginFolder.getPath());
                System.out.println("Registered plugin " + pastefyPlugin.config.name);
                plugins.put(pastefyPlugin.config.name, pastefyPlugin);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void setupModels() {
        try {
            Supplier<SQL> factory;
            String driverName = config.get("database.driver", "none");
            switch (driverName) {
                case "mysql": {
                    factory = () -> {
                        MySQL sql = new MySQL(
                                config.get("database.host", "localhost"),
                                config.getInt("database.port", 3306),
                                config.get("database.name", "app"),
                                config.get("database.user", "root"),
                                config.get("database.password", "")
                        );

                        if (config.getObject("database.customparams") != null) {
                            config.getObject("database.customparams").forEach((key, value) -> {
                                sql.setCustomParam(key, value.string());
                            });
                        }
                        return sql;
                    };


                    break;
                }
                case "sqlite": {
                    factory = () -> new SQLite(config.get("database.file", "sb.sqlite"));
                    break;
                }
                default: {
                    throw new ORMConfigurationException("Unknown database driver " + driverName);
                }
            }


            SQLPool sqlPool = new SQLPool(new MinMaxScaler(config.getInt("database.pool.min", 10), config.getInt("database.pool.max", 10)), factory);
            Handler handler = new ConsoleHandler();
            handler.setLevel(Level.ALL);
            Logger.getLogger("ORM").addHandler(handler);
            Logger.getLogger("ORM").setLevel(Level.ALL);
            ORMConfig ormConfig = new ORMConfig()
                    .setTablePrefix("pastefy_")
                    .addTypeMapper(new AbstractDataTypeMapper());

            ORM.register(Paste.class.getPackage(), sqlPool, ormConfig);

            ORM.autoMigrate();
        } catch (ORMConfigurationException e) {
            e.printStackTrace();
        }

        getBackendPlugins().forEach(PastefyBackendPlugin::setupModels);
    }

    protected void setupPassport() {
        passport = new Passport("/api/v2/auth");
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

        getBackendPlugins().forEach(PastefyBackendPlugin::setupPassport);
        passport.use("oauth2", oAuth2Strategy);
    }

    protected void setupServer() {
        httpRouter.port(config.getInt("http.server.port", 80));

        if (config.has("http.server.cors")) {
            httpRouter.beforeInterceptor(new CORSPolicy(config.get("http.server.cors")));
        }

        httpRouter.beforeInterceptor(new MultipartPolicy(config.get("http.server.tmp", null)));
        httpRouter.responseTransformer(new SerializedResponseTransformer().ignoreStrings());

        oAuth2Strategy.getProviders().forEach((name, authService) -> {
            System.out.println("ADDED " + name + " as auth provider");
        });

        httpRouter.exceptionHandler((exchange, throwable) -> {
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
        httpRouter.middleware("auth", new AuthMiddleware());
        httpRouter.middleware("admin", new AdminMiddleware());


        httpRouter.middleware("blocked-check", new BlockedMiddleware());
        httpRouter.middleware("awaiting-access-check", new AwaitingAccessMiddleware());

        httpRouter.middleware("auth-login-required-read", exchange -> {
            if (loginRequiredForRead && (exchange.attrib("user") == null || !((User) exchange.attrib("user")).roleCheck()))
                throw new AuthenticationException();
            return null;
        });
        httpRouter.middleware("auth-login-required-create", exchange -> {
            if (loginRequiredForCreate && (exchange.attrib("user") == null || !((User) exchange.attrib("user")).roleCheck()))
                throw new AuthenticationException();
            return null;
        });

        httpRouter.middleware("public-pastes-endpoint", exchange -> {
            if (!publicPastesEnabled)
                throw new FeatureDisabledException();
            return null;
        });

        if (getConfig().has("ratelimiter.millis"))
            httpRouter.middleware("rate-limiter", new RateLimitMiddleware(getConfig().getInt("ratelimiter.millis", 5000), getConfig().getInt("ratelimiter.limit", 5)).createAutoDeadRateLimitsRemover(1000 * 60 * 10));
        else
            httpRouter.middleware("rate-limiter", e -> null);

        httpRouter.middleware("image-rate-limiter", new RateLimitMiddleware(5000, 3).createAutoDeadRateLimitsRemover(1000 * 60 * 10));

        httpRouter.beforeInterceptor(exchange -> {
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
                if (!oAuth2Strategy.getProviders().isEmpty() && authKey != null) {
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

        List<PastefyPlugin> list = plugins.values().stream().filter(p -> p.config.frontend != null).collect(Collectors.toList());
        list.forEach(p -> {
            if (p.config.frontend.folder != null && p.config.frontend.entrypoint != null) {
                System.out.println("Registered plugin " + p.config.name + " frontend directory");
                System.out.println("- Public: " + p.getPublicFolder());
                System.out.println("- Path: " + p.path + "/" + p.config.frontend.folder);
                httpRouter.staticDirectory(p.getPublicFolder(), p.path + "/" + p.config.frontend.folder);
            }
        });


        try {
            homeHTML = FileUtils.readFile(getClass().getClassLoader().getResourceAsStream("static/index.html"));

            StringBuilder registerPlugins = new StringBuilder();
            list.forEach(p -> {
                if (p.config.frontend != null && p.config.frontend.folder != null && p.config.frontend.entrypoint != null) {
                    registerPlugins
                            .append("{")
                            .append("\"config\": ")
                            .append(new Gson().toJson(p.config))
                            .append(", \"entrypoint\": \"")
                            .append(p.getPublicFolder())
                            .append("/")
                            .append(p.config.frontend.entrypoint)
                            .append('"')
                            .append("},\n");
                }
            });

            String customHeaders = config.get("pastefy.customheader", "");
            String customBody = config.get("pastefy.custombody", "");

            homeHTML = homeHTML
                    .replace("/*PASTEFY_PLUGINS*/", registerPlugins.toString())
                    .replaceAll("<!--CUSTOM_HEADERS-->", customHeaders)
                    .replaceAll("<!--CUSTOM_BODY-->", customBody);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        RequestHandler requestHandler = exchange -> {
            if (homeHTML == null) {
                throw new NotFoundException();
            }

            exchange.header("Content-Type", "text/html; charset=UTF-8");
            exchange.write(homeHTML);
            return "";
        };

        httpRouter.controller(HttpController.class, AppController.class.getPackage());

        passport.createRoutes(httpRouter);

        getBackendPlugins().forEach(PastefyBackendPlugin::beforeRoutes);

        httpRouter.get("/", requestHandler);
        httpRouter.staticResourceDirectory("/", "static");

        httpRouter.get("/api/{*:path}", e -> {
            throw new NotFoundException();
        });

        httpRouter.get("/{*:path}", requestHandler);

        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            public void run() {
                // Delete expired pastes
                Repo.get(Paste.class)
                        .where("expireAt", "<", Timestamp.from(Instant.now()))
                        .whereNotNull("expireAt")
                        .delete();
            }
        }, 0, 1000 * 60);
        getBackendPlugins().forEach(PastefyBackendPlugin::setupServer);
    }

    public void start() {
        httpRouter.start();
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

    public PasteAI getPasteAI() {
        return pasteAI;
    }
    public boolean aiEnabled() {
        return pasteAI != null;
    }

    public String getHomeHTML() {
        return homeHTML;
    }

    public List<PastefyBackendPlugin> getBackendPlugins() {
        return plugins.values().stream()
                .filter(p -> p.backendPlugin != null)
                .map(p -> p.backendPlugin)
                .collect(Collectors.toList());
    }
}
