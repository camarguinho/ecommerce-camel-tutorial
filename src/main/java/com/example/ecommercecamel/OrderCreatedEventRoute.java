package com.example.ecommercecamel;

import org.apache.camel.builder.RouteBuilder;

/**
 * Publica o evento de pedido criado para o destino de integracao configurado.
 */
public class OrderCreatedEventRoute extends RouteBuilder {

    @Override
    public void configure() {
        from("direct:publish-order-created")
                .routeId("publish-order-created-route")
                .setHeader("kafka.KEY", simple("${body.orderId}"))
                .marshal().json()
                .toD("{{order.created.endpoint}}");
    }
}