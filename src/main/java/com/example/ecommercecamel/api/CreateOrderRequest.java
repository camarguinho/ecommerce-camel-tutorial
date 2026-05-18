package com.example.ecommercecamel.api;

import java.util.List;

/**
 * Representa a carga de entrada para criacao de um pedido.
 */
public record CreateOrderRequest(
        String customerId,
        boolean simulatePaymentFailure,
        List<OrderItemRequest> items) {
}