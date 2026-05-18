package com.example.ecommercecamel;

import java.util.Map;
import org.apache.camel.builder.RouteBuilder;

/**
 * Responde aos endpoints basicos de descoberta e health de forma independente do transporte HTTP.
 */
public class HealthRoute extends RouteBuilder {

    @Override
    public void configure() {
    from("direct:service-index")
        .routeId("service-index-route")
        .setBody(constant(Map.of(
            "service", "ecommerce-camel-tutorial",
            "status", "UP",
            "endpoints", Map.of(
                "health", "/health",
                "orders", "/api/orders"))))
        .marshal().json();

        from("direct:health-check")
                .routeId("health-check-route")
                .setBody(constant(Map.of("status", "UP", "service", "ecommerce-camel-tutorial")))
                .marshal().json();
    }
}