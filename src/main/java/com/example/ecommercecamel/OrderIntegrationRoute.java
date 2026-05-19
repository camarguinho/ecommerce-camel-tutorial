package com.example.ecommercecamel;

import com.example.ecommercecamel.domain.OrderCreatedEvent;
import java.time.Instant;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

/**
 * Consome o evento publicado em Kafka e o persiste em H2.
 */
public class OrderIntegrationRoute extends RouteBuilder {

    @Override
    public void configure() {
        onException(Exception.class)
            .maximumRedeliveries("{{order.created.retry.max:3}}")
            .redeliveryDelay("{{order.created.retry.delay.ms:1000}}")
            .retryAttemptedLogLevel(LoggingLevel.WARN)
            .retriesExhaustedLogLevel(LoggingLevel.ERROR)
            .useOriginalMessage()
            .handled(true)
            .to("direct:order-created-dlt");

        from("{{order.created.consumer.endpoint}}")
                .routeId("consume-order-created-route")
                .process(exchange -> {
                    String payload = exchange.getIn().getBody(String.class);
                    exchange.setProperty("orderCreatedRawPayload", payload);
                })
                .unmarshal().json(OrderCreatedEvent.class)
                .setProperty("orderCreatedEvent", body())
                .setHeader("orderId", simple("${body.orderId}"))
                .setHeader("customerId", simple("${body.customerId}"))
                .setHeader("totalAmount", simple("${body.totalAmount}"))
                .setHeader("status", constant("RECEIVED"))
                .process(exchange -> exchange.getMessage().setHeader("receivedAt", Instant.now().toString()))
                .setHeader("payload", exchangeProperty("orderCreatedRawPayload"))
                .toD("{{order.created.persistence.endpoint}}")
                .log("Order ${header.orderId} stored in H2 after Kafka consumption");

        from("direct:order-created-dlt")
                .routeId("order-created-dlt-route")
                .process(exchange -> {
                    Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                    OrderCreatedEvent event = exchange.getProperty("orderCreatedEvent", OrderCreatedEvent.class);
                    String orderId = event != null ? event.orderId() : exchange.getMessage().getHeader("orderId", String.class);
                    exchange.getMessage().setHeader("eventId", orderId);
                    exchange.getMessage().setHeader("failedAt", Instant.now().toString());
                    exchange.getMessage().setHeader("failureReason", exception == null ? "Unknown failure" : exception.getMessage());
                    exchange.getMessage().setHeader("payload", exchange.getIn().getBody(String.class));
                })
                .toD("{{order.created.dlt.endpoint}}")
                .log(LoggingLevel.ERROR, "OrderCreatedEvent moved to DLT with reason ${header.failureReason}");
    }
}