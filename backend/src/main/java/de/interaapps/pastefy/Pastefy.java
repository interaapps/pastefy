package de.interaapps.pastefy;

import de.interaapps.pastefy.auth.AuthMiddleware;
import de.interaapps.pastefy.auth.AuthenticationProvider;
import de.interaapps.pastefy.auth.IAAuthProvider;
import de.interaapps.pastefy.commands.PasteCommand;
import de.interaapps.pastefy.controller.PasteController;
import de.interaapps.pastefy.model.auth.User;
import de.interaapps.pastefy.model.database.AuthKey;
import de.interaapps.pastefy.model.database.Paste;
import de.interaapps.pastefy.model.responses.ExceptionResponse;
import org.javawebstack.command.CommandSystem;
import org.javawebstack.framework.HttpController;
import org.javawebstack.framework.WebApplication;
import org.javawebstack.framework.config.Config;
import org.javawebstack.httpclient.HTTPClient;
import org.javawebstack.httpserver.HTTPServer;
import org.javawebstack.httpserver.handler.RequestHandler;
import org.javawebstack.httpserver.handler.WebSocketHandler;
import org.javawebstack.httpserver.websocket.WebSocket;
import org.javawebstack.orm.ORM;
import org.javawebstack.orm.ORMConfig;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.exception.ORMConfigurationException;
import org.javawebstack.orm.wrapper.SQL;
import sun.misc.IOUtils;

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
        ORMConfig config = new ORMConfig().setTablePrefix("pastefy_");
        ORM.register(Paste.class.getPackage(), sql, config);
        ORM.autoMigrate();
    }

    protected void setupServer(HTTPServer server) {
        String authProvider = getConfig().get("auth.provider", "none");
        if (authProvider.equalsIgnoreCase("INTERAAPPSAUTH"))
            authenticationProvider = new IAAuthProvider(getConfig().get("interaapps.auth.key"));
        else
            authenticationProvider = null;

        if (authenticationProvider != null)
            getInjector().setInstance(AuthenticationProvider.class, authenticationProvider);

        server.exceptionHandler((exchange, throwable) -> new ExceptionResponse(throwable));
        server.middleware("auth", new AuthMiddleware());
        server.beforeInterceptor(exchange -> {
            exchange.header("Server", "InteraApps-Pastefy");

            exchange.attrib("loggedIn", false);
            exchange.attrib("user", null);
            if (exchange.header("x-auth-key") != null) {
                AuthKey authKey = Repo.get(AuthKey.class).where("key", exchange.header("x-auth-key")).get();
                if (authenticationProvider != null && authKey != null) {
                    User user = authenticationProvider.getUser(authKey);

                    if (user != null) {
                        exchange.attrib("loggedIn", true);
                        exchange.attrib("user", user);
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

    protected void setupCommands(CommandSystem commandSystem) {
        commandSystem.addCommand("paste", new PasteCommand());
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
