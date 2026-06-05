package com.example.ecommercecamel.route;

import org.apache.camel.builder.RouteBuilder;

/**
 * Define uma rota minima de inicializacao para validar o bootstrap do projeto.
 */
public class StartupRoute extends RouteBuilder {

    /**
     * Configura uma rota simples usada para provar que o runtime Camel foi montado corretamente.
     */
    @Override
    public void configure() {
        from("direct:startup-check")
                .routeId("startup-check-route")
                .setBody(constant("camel-ready"));
    }
}