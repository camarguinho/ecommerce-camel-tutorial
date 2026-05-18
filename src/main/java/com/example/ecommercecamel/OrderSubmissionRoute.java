package com.example.ecommercecamel;

import com.example.ecommercecamel.api.CreateOrderRequest;
import com.example.ecommercecamel.api.OrderAcceptedResponse;
import com.example.ecommercecamel.api.OrderItemRequest;
import com.example.ecommercecamel.domain.OrderCreatedEvent;
import com.example.ecommercecamel.domain.OrderLine;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Recebe pedidos, valida a carga e produz o primeiro evento interno do fluxo.
 */
public class OrderSubmissionRoute extends RouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderSubmissionRoute.class);

    @Override
    public void configure() {
        from("direct:submit-order")
                .routeId("submit-order-route")
                .log("Receiving order submission for customer ${body.customerId}")
                .process(this::createOrder)
                .setBody(exchangeProperty("orderCreatedEvent"))
                .to("direct:publish-order-created")
                .setBody(exchangeProperty("orderAcceptedResponse"));
    }

    private void createOrder(Exchange exchange) {
        CreateOrderRequest request = exchange.getMessage().getBody(CreateOrderRequest.class);
        validate(request);

        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                request.customerId(),
                request.simulatePaymentFailure(),
                mapItems(request.items()),
                calculateTotal(request.items()));

        exchange.setProperty("orderCreatedEvent", orderCreatedEvent);
        OrderAcceptedResponse acceptedResponse = new OrderAcceptedResponse(
                orderCreatedEvent.orderId(),
                "RECEIVED",
            orderCreatedEvent.totalAmount());
        exchange.setProperty("orderAcceptedResponse", acceptedResponse);
        exchange.getMessage().setBody(acceptedResponse);
        LOGGER.info("Order {} accepted with total {}",
            orderCreatedEvent.orderId(),
            orderCreatedEvent.totalAmount());
    }

    private void validate(CreateOrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (request.customerId() == null || request.customerId().isBlank()) {
            throw new IllegalArgumentException("customerId is required");
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("At least one item is required");
        }
        for (OrderItemRequest item : request.items()) {
            if (item.productId() == null || item.productId().isBlank()) {
                throw new IllegalArgumentException("productId is required");
            }
            if (item.quantity() <= 0) {
                throw new IllegalArgumentException("quantity must be greater than zero");
            }
            if (item.unitPrice() == null || item.unitPrice().signum() < 0) {
                throw new IllegalArgumentException("unitPrice must be zero or greater");
            }
        }
    }

    private List<OrderLine> mapItems(List<OrderItemRequest> items) {
        return items.stream()
                .map(item -> new OrderLine(item.productId(), item.quantity(), item.unitPrice()))
                .toList();
    }

    private BigDecimal calculateTotal(List<OrderItemRequest> items) {
        return items.stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}