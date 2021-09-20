package de.interaapps.pastefy;

import de.interaapps.accounts.apiclient.AccountsClient;
import de.interaapps.pastefy.auth.AuthMiddleware;
import de.interaapps.pastefy.controller.PasteController;
import de.interaapps.pastefy.model.database.AuthKey;
import de.interaapps.pastefy.model.database.Paste;
import de.interaapps.pastefy.model.database.User;
import de.interaapps.pastefy.model.responses.ExceptionResponse;
import org.javawebstack.framework.HttpController;
import org.javawebstack.framework.WebApplication;
import org.javawebstack.framework.config.Config;
import org.javawebstack.httpclient.HTTPClient;
import org.javawebstack.httpserver.HTTPServer;
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
        Map<String, Class<? extends OAuth2Service>> oauthServicesClasses = new HashMap<>();
        oauthServicesClasses.put("interaapps", InteraAppsOAuth2Service.class);
        oauthServicesClasses.put("github", GithubOAuth2Service.class);
        oauthServicesClasses.put("google", GoogleOAuth2Service.class);
        oauthServicesClasses.put("twitch", TwitchOAuth2Service.class);
        oauthServicesClasses.put("discord", DiscordOAuth2Service.class);
        oAuth2Module.setOAuth2Callback((s, exchange, oAuth2Callback) -> {
            User.AuthenticationProvider provider = de.interaapps.pastefy.model.database.User.AuthenticationProvider.getProviderByClass(oauthServicesClasses.get(s));
            Profile profile = oAuth2Callback.getProfile();

            User user = Repo.get(User.class).where("authId", profile.getId()).where("authProvider", provider).first();

            if (user == null) {
                user = new de.interaapps.pastefy.model.database.User();
                int i = 1;
                final String uniqueName = profile.getName().replaceAll("[^a-zA-Z0-9]", "");
                user.uniqueName = uniqueName;
                while (Repo.get(de.interaapps.pastefy.model.database.User.class).where("uniqueName", user.uniqueName).first() != null) {
                    user.uniqueName = uniqueName+i++;
                }
                user.authId = profile.getId();
                user.authProvider = provider;
            }
            // On every login the username, avatar and e-mail gets updated
            user.name = profile.getName();
            user.avatar = profile.getAvatar();
            user.eMail = profile.getMail();
            user.save();

            AuthKey authKey = new AuthKey();
            authKey.refreshToken = oAuth2Callback.getRefreshToken();
            authKey.accessToken = oAuth2Callback.getToken();
            authKey.userId = user.id;
            authKey.type = AuthKey.Type.USER;
            authKey.save();

            exchange.redirect("/auth?key=" + authKey.getKey());
            return "";
        });

        server.exceptionHandler((exchange, throwable) -> new ExceptionResponse(throwable));
        server.middleware("auth", new AuthMiddleware());
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
