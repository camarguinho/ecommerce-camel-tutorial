package com.example.ecommercecamel;

import java.util.Map;
import org.apache.camel.Exchange;
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
                    "orders", "/api/orders",
                    "h2Console", BootstrapProperties.get("h2.console.path", "/h2-console")))))
            .marshal().json();

        from("direct:health-check")
                .routeId("health-check-route")
                .setBody(constant(Map.of("status", "UP", "service", "ecommerce-camel-tutorial")))
                .marshal().json();

        from("direct:h2-console-redirect")
                .routeId("h2-console-redirect-route")
                .process(exchange -> {
                    String hostHeader = exchange.getMessage().getHeader("Host", "localhost", String.class);
                    String host = hostHeader.contains(":") ? hostHeader.substring(0, hostHeader.indexOf(':')) : hostHeader;
                    String scheme = exchange.getMessage().getHeader("X-Forwarded-Proto", "http", String.class);
                    String location = String.format(
                            "%s://%s:%s/",
                            scheme,
                            host,
                            BootstrapProperties.get("h2.console.port", "8082"));
                    exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 302);
                    exchange.getMessage().setHeader("Location", location);
                    exchange.getMessage().setBody("");
                });
    }
}