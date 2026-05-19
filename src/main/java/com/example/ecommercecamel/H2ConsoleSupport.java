package com.example.ecommercecamel;

import org.h2.tools.Server;

/**
 * Gerencia o console web do H2 para inspecao local do banco em memoria.
 */
public final class H2ConsoleSupport implements AutoCloseable {

    private final Server webServer;

    private H2ConsoleSupport(Server webServer) {
        this.webServer = webServer;
    }

    public static H2ConsoleSupport start() throws Exception {
        if (!isEnabled()) {
            return new H2ConsoleSupport(null);
        }

        Server server = Server.createWebServer(
                "-web",
                "-webAllowOthers",
                "-webPort",
                property("h2.console.port", "8082"))
                .start();
        return new H2ConsoleSupport(server);
    }

    public String getUrl() {
        if (webServer == null) {
            return null;
        }
        return webServer.getURL();
    }

    @Override
    public void close() {
        if (webServer != null) {
            webServer.stop();
        }
    }

    private static boolean isEnabled() {
        return Boolean.parseBoolean(property("h2.console.enabled", "true"));
    }

    private static String property(String key, String defaultValue) {
        return System.getProperty(key, defaultValue);
    }
}