package com.example.ecommercecamel;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

/**
 * Define a exposicao HTTP dos endpoints basicos de descoberta e health.
 */
public class HealthHttpRoute extends RouteBuilder {

    @Override
    public void configure() {
        restConfiguration()
                .component("platform-http")
                .host("0.0.0.0")
                .port("{{http.port:8080}}")
                .bindingMode(RestBindingMode.off);

        from("platform-http:/")
            .routeId("service-index-http-route")
            .to("direct:service-index");

        from("platform-http:{{h2.console.path:/h2-console}}")
            .routeId("h2-console-http-route")
            .to("direct:h2-console-redirect");

        rest("/health")
                .get()
                .produces("application/json")
                .to("direct:health-check");
    }
}