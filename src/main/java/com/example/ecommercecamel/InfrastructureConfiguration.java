package com.example.ecommercecamel;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

/**
 * Cria a infraestrutura basica usada pelas rotas de integracao.
 */
public final class InfrastructureConfiguration {

    private InfrastructureConfiguration() {
    }

    public static DataSource createDataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("org.postgresql.Driver");
        hikariConfig.setJdbcUrl(String.format(
                "jdbc:postgresql://%s:%s/%s",
                property("postgres.host", "localhost"),
                property("postgres.port", "5432"),
                property("postgres.database", "ecommerce")));
        hikariConfig.setUsername(property("postgres.username", "ecommerce"));
        hikariConfig.setPassword(property("postgres.password", "ecommerce"));
        hikariConfig.setMaximumPoolSize(5);
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setPoolName("orders-hikari-pool");
        return new HikariDataSource(hikariConfig);
    }

    public static void migrate(DataSource dataSource) throws Exception {
        try (var connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new Liquibase(
                    "db/changelog/db.changelog-master.xml",
                    new ClassLoaderResourceAccessor(),
                    database);
            liquibase.update(new Contexts(), new LabelExpression());
        }
    }

    private static String property(String key, String defaultValue) {
        return System.getProperty(key, defaultValue);
    }
}