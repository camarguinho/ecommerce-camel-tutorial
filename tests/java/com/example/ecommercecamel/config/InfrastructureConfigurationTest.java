package com.example.ecommercecamel.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Valida o bootstrap da persistencia local usando H2 e Liquibase.
 */
class InfrastructureConfigurationTest {

    @Test
    void shouldReadH2DefaultsFromApplicationProperties() throws Exception {
        String previousUrl = System.getProperty("h2.datasource.url");
        String previousUsername = System.getProperty("h2.datasource.username");
        String previousPassword = System.getProperty("h2.datasource.password");

        System.clearProperty("h2.datasource.url");
        System.clearProperty("h2.datasource.username");
        System.clearProperty("h2.datasource.password");

        try (HikariDataSource dataSource = (HikariDataSource) InfrastructureConfiguration.createDataSource()) {
            assertThat(dataSource.getJdbcUrl()).isEqualTo(InfrastructureConfiguration.DEFAULT_H2_JDBC_URL);
            assertThat(dataSource.getUsername()).isEqualTo(InfrastructureConfiguration.DEFAULT_H2_USERNAME);
            assertThat(dataSource.getPassword()).isEqualTo(InfrastructureConfiguration.DEFAULT_H2_PASSWORD);
        } finally {
            restoreProperty("h2.datasource.url", previousUrl);
            restoreProperty("h2.datasource.username", previousUsername);
            restoreProperty("h2.datasource.password", previousPassword);
        }
    }

    @Test
    void shouldAllowSystemPropertiesToOverrideApplicationProperties() throws Exception {
        String previousUrl = System.getProperty("h2.datasource.url");
        String previousUsername = System.getProperty("h2.datasource.username");
        String previousPassword = System.getProperty("h2.datasource.password");

        System.setProperty("h2.datasource.url", "jdbc:h2:mem:override-webshop;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        System.setProperty("h2.datasource.username", "custom-user");
        System.setProperty("h2.datasource.password", "custom-password");

        try (HikariDataSource dataSource = (HikariDataSource) InfrastructureConfiguration.createDataSource()) {
            assertThat(dataSource.getJdbcUrl()).isEqualTo("jdbc:h2:mem:override-webshop;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
            assertThat(dataSource.getUsername()).isEqualTo("custom-user");
            assertThat(dataSource.getPassword()).isEqualTo("custom-password");
        } finally {
            restoreProperty("h2.datasource.url", previousUrl);
            restoreProperty("h2.datasource.username", previousUsername);
            restoreProperty("h2.datasource.password", previousPassword);
        }
    }

    @Test
    void shouldCreateH2SchemaWithLiquibase() throws Exception {
        String databaseName = "webshop-" + UUID.randomUUID();
        String jdbcUrl = "jdbc:h2:mem:" + databaseName + ";DB_CLOSE_DELAY=-1;MODE=PostgreSQL";
        String previousUrl = System.getProperty("h2.datasource.url");

        System.setProperty("h2.datasource.url", jdbcUrl);

        try (HikariDataSource dataSource = (HikariDataSource) InfrastructureConfiguration.createDataSource()) {
            InfrastructureConfiguration.migrate(dataSource);

            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                assertThat(exists(statement, "SELECT 1 FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = 'ECOMMERCE'"))
                    .isTrue();
                assertThat(exists(statement, "SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'ECOMMERCE' AND TABLE_NAME = 'ORDER_EVENTS'"))
                        .isTrue();
                assertThat(exists(statement, "SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'ECOMMERCE' AND TABLE_NAME = 'ORDER_EVENT_DLT'"))
                        .isTrue();
                assertThat(exists(statement, "SELECT 1 FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_SCHEMA = 'ECOMMERCE' AND TABLE_NAME = 'ORDER_EVENT_SUMMARY'"))
                        .isTrue();
            }
        } finally {
            if (previousUrl == null) {
                System.clearProperty("h2.datasource.url");
            } else {
                System.setProperty("h2.datasource.url", previousUrl);
            }
        }
    }

    private boolean exists(Statement statement, String sql) throws Exception {
        try (ResultSet resultSet = statement.executeQuery(sql)) {
            return resultSet.next();
        }
    }

    private void restoreProperty(String key, String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }
}