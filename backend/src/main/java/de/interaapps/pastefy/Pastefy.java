package de.interaapps.pastefy;

import de.interaapps.accounts.apiclient.AccountsClient;
import de.interaapps.pastefy.auth.AuthMiddleware;
import de.interaapps.pastefy.auth.AuthenticationProvider;
import de.interaapps.pastefy.auth.IAAuthProvider;
import de.interaapps.pastefy.controller.PasteController;
import de.interaapps.pastefy.model.auth.User;
import de.interaapps.pastefy.model.database.AuthKey;
import de.interaapps.pastefy.model.database.Paste;
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
    private AuthenticationProvider authenticationProvider;
    private HTTPClient iaAuthClient;

    public static Pastefy getInstance() {
        return instance;
    }

    protected void setupConfig(Config config) {
        Map<String, String> map = new HashMap<>();
        map.put("INTERAAPPS_AUTH_KEY", "interaapps.auth.key");
        map.put("INTERAAPPS_AUTH_ID", "interaapps.auth.id");
        map.put("AUTH_PROVIDER", "auth.provider");

        config.addEnvKeyMapping(map);
        config.addEnvFile(new File(".env"));
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
        String authProvider = getConfig().get("auth.provider", "NONE");
        if (authProvider.equalsIgnoreCase("INTERAAPPSAUTH"))
            authenticationProvider = new IAAuthProvider(getConfig().get("interaapps.auth.key"));
        else
            authenticationProvider = null;

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
                if (authenticationProvider != null && authKey != null) {
                    User user = authenticationProvider.getUser(authKey);

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

    public static void main(String[] args) {
        instance = new Pastefy();
        instance.run(args);
    }

    public HTTPClient getIaAuthClient() {
        return iaAuthClient;
    }

    public AuthenticationProvider getAuthenticationProvider() {
        return authenticationProvider;
    }
}
