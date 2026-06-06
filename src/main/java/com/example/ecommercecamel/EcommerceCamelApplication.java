package com.example.ecommercecamel;

import com.example.ecommercecamel.config.InfrastructureConfiguration;
import com.example.ecommercecamel.route.HealthHttpRoute;
import com.example.ecommercecamel.route.HealthRoute;
import com.example.ecommercecamel.route.OrderApiRoute;
import com.example.ecommercecamel.route.OrderCreatedEventRoute;
import com.example.ecommercecamel.route.OrderHttpRoute;
import com.example.ecommercecamel.route.OrderIntegrationRoute;
import com.example.ecommercecamel.route.OrderSubmissionRoute;
import com.example.ecommercecamel.route.StartupRoute;
import com.example.ecommercecamel.support.H2ConsoleSupport;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.apache.camel.main.Main;

/**
 * Bootstrap principal da aplicacao Camel standalone.
 */
public final class EcommerceCamelApplication {

    private EcommerceCamelApplication() {
    }

    public static void main(String[] args) throws Exception {
        DataSource dataSource = InfrastructureConfiguration.createDataSource();
        InfrastructureConfiguration.migrate(dataSource);

        try (H2ConsoleSupport h2ConsoleSupport = H2ConsoleSupport.start()) {
            Main main = new Main();
            main.bind("ordersDataSource", dataSource);
            main.configure().addRoutesBuilder(new StartupRoute());
            main.configure().addRoutesBuilder(new HealthRoute());
            main.configure().addRoutesBuilder(new HealthHttpRoute());
            main.configure().addRoutesBuilder(new OrderApiRoute());
            main.configure().addRoutesBuilder(new OrderSubmissionRoute());
            main.configure().addRoutesBuilder(new OrderCreatedEventRoute());
            main.configure().addRoutesBuilder(new OrderIntegrationRoute());
            main.configure().addRoutesBuilder(new OrderHttpRoute());
            main.run(args);
        } finally {
            if (dataSource instanceof HikariDataSource hikariDataSource) {
                hikariDataSource.close();
            }
        }
    }
}