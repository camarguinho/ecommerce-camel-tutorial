package com.example.ecommercecamel;

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
    void shouldCreateH2SchemaWithLiquibase() throws Exception {
        String databaseName = "ecommerce-" + UUID.randomUUID();
        String jdbcUrl = "jdbc:h2:mem:" + databaseName + ";DB_CLOSE_DELAY=-1;MODE=PostgreSQL";
        String previousUrl = System.getProperty("h2.datasource.url");

        System.setProperty("h2.datasource.url", jdbcUrl);

        try (HikariDataSource dataSource = (HikariDataSource) InfrastructureConfiguration.createDataSource()) {
            InfrastructureConfiguration.migrate(dataSource);

            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                assertThat(exists(statement, "SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'ORDER_EVENTS'"))
                        .isTrue();
                assertThat(exists(statement, "SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'ORDER_EVENT_DLT'"))
                        .isTrue();
                assertThat(exists(statement, "SELECT 1 FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_NAME = 'ORDER_EVENT_SUMMARY'"))
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
}