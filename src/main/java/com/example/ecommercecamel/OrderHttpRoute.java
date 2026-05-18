package com.example.ecommercecamel;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

/**
 * Define apenas a exposicao HTTP do endpoint de pedidos.
 */
public class OrderHttpRoute extends RouteBuilder {

    @Override
    public void configure() {
        restConfiguration()
                .component("platform-http")
                .host("0.0.0.0")
                .port("{{http.port:8080}}")
                .bindingMode(RestBindingMode.off);

        rest("/api/orders")
                .post()
                .consumes("application/json")
                .produces("application/json")
                .to("direct:submit-order-rest");
    }
}