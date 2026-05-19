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

    public static final String DEFAULT_H2_JDBC_URL = "jdbc:h2:mem:ecommerce;DB_CLOSE_DELAY=-1;MODE=PostgreSQL";
    public static final String DEFAULT_H2_USERNAME = "sa";
    public static final String DEFAULT_H2_PASSWORD = "";

    private InfrastructureConfiguration() {
    }

    public static DataSource createDataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("org.h2.Driver");
        hikariConfig.setJdbcUrl(property("h2.datasource.url", DEFAULT_H2_JDBC_URL));
        hikariConfig.setUsername(property("h2.datasource.username", DEFAULT_H2_USERNAME));
        hikariConfig.setPassword(property("h2.datasource.password", DEFAULT_H2_PASSWORD));
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