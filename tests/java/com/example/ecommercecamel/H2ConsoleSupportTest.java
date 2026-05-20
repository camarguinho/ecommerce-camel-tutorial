package com.example.ecommercecamel;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class H2ConsoleSupportTest {

    @Test
    void shouldPrepareConsolePropertiesWithApplicationDatabaseConnection() throws Exception {
        Path directory = H2ConsoleSupport.prepareServerProperties();

        try {
            Path propertiesFile = directory.resolve(H2ConsoleSupport.SERVER_PROPERTIES_FILE);
            Properties properties = new Properties();
            properties.load(Files.newInputStream(propertiesFile));

            assertThat(properties.getProperty("webPort")).isEqualTo("8082");
            assertThat(properties.getProperty("webAllowOthers")).isEqualTo("true");
            assertThat(properties.getProperty("0")).isEqualTo(
                    "Webshop H2 (Embedded)|org.h2.Driver|jdbc:h2:mem:webshop;DB_CLOSE_DELAY=-1;MODE=PostgreSQL|sa");
        } finally {
            Files.deleteIfExists(directory.resolve(H2ConsoleSupport.SERVER_PROPERTIES_FILE));
            Files.deleteIfExists(directory);
        }
    }
}