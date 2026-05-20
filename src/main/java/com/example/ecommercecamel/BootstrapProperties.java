package com.example.ecommercecamel;

import java.io.InputStream;
import java.util.Properties;

/**
 * Resolve propriedades de bootstrap antes do Main do Camel iniciar.
 */
public final class BootstrapProperties {

    private static final String APPLICATION_PROPERTIES = "application.properties";
    private static final Properties FILE_PROPERTIES = loadFileProperties();

    private BootstrapProperties() {
    }

    public static String get(String key, String defaultValue) {
        String systemValue = System.getProperty(key);
        if (systemValue != null) {
            return systemValue;
        }

        return FILE_PROPERTIES.getProperty(key, defaultValue);
    }

    private static Properties loadFileProperties() {
        Properties properties = new Properties();

        try (InputStream inputStream = BootstrapProperties.class.getClassLoader()
                .getResourceAsStream(APPLICATION_PROPERTIES)) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Nao foi possivel carregar application.properties", exception);
        }

        return properties;
    }
}