package com.example.ecommercecamel.support;

import com.example.ecommercecamel.config.InfrastructureConfiguration;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Properties;
import java.util.stream.Stream;
import org.h2.tools.Server;

/**
 * Gerencia o console web do H2 para inspecao local do banco em memoria.
 */
public final class H2ConsoleSupport implements AutoCloseable {

    static final String SERVER_PROPERTIES_FILE = ".h2.server.properties";

    private final Server webServer;
    private final Path serverPropertiesDir;

    private H2ConsoleSupport(Server webServer, Path serverPropertiesDir) {
        this.webServer = webServer;
        this.serverPropertiesDir = serverPropertiesDir;
    }

    public static H2ConsoleSupport start() throws Exception {
        if (!isEnabled()) {
            return new H2ConsoleSupport(null, null);
        }

        Path serverPropertiesDir = prepareServerProperties();
        Server server = Server.createWebServer(
                "-web",
                "-webAllowOthers",
                "-properties",
                serverPropertiesDir.toString(),
                "-webPort",
                BootstrapProperties.get("h2.console.port", "8082"))
                .start();
        return new H2ConsoleSupport(server, serverPropertiesDir);
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
        deleteServerPropertiesDir();
    }

    private static boolean isEnabled() {
        return Boolean.parseBoolean(BootstrapProperties.get("h2.console.enabled", "true"));
    }

    static Path prepareServerProperties() throws IOException {
        Path directory = Files.createTempDirectory("h2-console-");
        Path propertiesFile = directory.resolve(SERVER_PROPERTIES_FILE);
        Properties properties = new Properties();
        properties.setProperty("webPort", BootstrapProperties.get("h2.console.port", "8082"));
        properties.setProperty("webAllowOthers", "true");
        properties.setProperty("0", defaultConnectionSetting());

        try (OutputStream outputStream = Files.newOutputStream(propertiesFile)) {
            properties.store(outputStream, "H2 Console bootstrap properties");
        }

        return directory;
    }

    private static String defaultConnectionSetting() {
        return String.join("|",
                BootstrapProperties.get("h2.console.connection.name", "Webshop H2 (Embedded)"),
                "org.h2.Driver",
                BootstrapProperties.get("h2.datasource.url", InfrastructureConfiguration.DEFAULT_H2_JDBC_URL),
                BootstrapProperties.get("h2.datasource.username", InfrastructureConfiguration.DEFAULT_H2_USERNAME));
    }

    private void deleteServerPropertiesDir() {
        if (serverPropertiesDir == null) {
            return;
        }

        try (Stream<Path> files = Files.walk(serverPropertiesDir)) {
            files.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException exception) {
                    throw new IllegalStateException("Nao foi possivel limpar as propriedades temporarias do H2", exception);
                }
            });
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel limpar as propriedades temporarias do H2", exception);
        }
    }
}