package com.example.ecommercecamel;

import org.apache.camel.CamelContext;
import org.apache.camel.component.platform.http.vertx.VertxPlatformHttpServer;
import org.apache.camel.component.platform.http.vertx.VertxPlatformHttpServerConfiguration;
import org.apache.camel.main.BaseMainSupport;
import org.apache.camel.main.Main;
import org.apache.camel.main.MainListenerSupport;

/**
 * Inicializa a aplicacao Apache Camel em modo standalone para o tutorial.
 */
public final class EcommerceCamelApplication {

    /**
     * Cria a aplicacao sem permitir instanciacao externa.
     */
    private EcommerceCamelApplication() {
    }

    /**
     * Inicializa o runtime Camel e registra as rotas base da aplicacao.
     *
     * @param args argumentos recebidos da linha de comando.
     * @throws Exception quando ocorre falha ao subir o runtime Camel.
     */
    public static void main(String[] args) throws Exception {
        Main main = new Main();
        var ordersDataSource = InfrastructureConfiguration.createDataSource();
        InfrastructureConfiguration.migrate(ordersDataSource);
        main.bind("ordersDataSource", ordersDataSource);
        main.addMainListener(new MainListenerSupport() {
            @Override
            public void beforeStart(BaseMainSupport mainSupport) {
                try {
                    mainSupport.getCamelContext().addService(createPlatformHttpServer(mainSupport.getCamelContext()), true, true);
                } catch (Exception exception) {
                    throw new IllegalStateException("Nao foi possivel inicializar o servidor HTTP standalone", exception);
                }
            }
        });
        main.configure().addRoutesBuilder(new StartupRoute());
        main.configure().addRoutesBuilder(new HealthHttpRoute());
        main.configure().addRoutesBuilder(new HealthRoute());
        main.configure().addRoutesBuilder(new OrderHttpRoute());
        main.configure().addRoutesBuilder(new OrderApiRoute());
        main.configure().addRoutesBuilder(new OrderSubmissionRoute());
        main.configure().addRoutesBuilder(new OrderCreatedEventRoute());
        main.configure().addRoutesBuilder(new OrderIntegrationRoute());
        main.run(args);
    }

    private static VertxPlatformHttpServer createPlatformHttpServer(CamelContext camelContext) throws Exception {
        VertxPlatformHttpServerConfiguration configuration = new VertxPlatformHttpServerConfiguration();
        String httpHost = camelContext.resolvePropertyPlaceholders("{{http.host:0.0.0.0}}");
        int httpPort = Integer.parseInt(camelContext.resolvePropertyPlaceholders("{{http.port:8080}}"));

        configuration.setHost(httpHost);
        configuration.setPort(httpPort);
        configuration.setBindHost(httpHost);
        configuration.setBindPort(httpPort);
        configuration.setPath("/");

        VertxPlatformHttpServer server = new VertxPlatformHttpServer(configuration);
        server.setCamelContext(camelContext);
        return server;
    }
}