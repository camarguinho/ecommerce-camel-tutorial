package com.example.ecommercecamel.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * Evento interno criado assim que o pedido eh aceito pela rota de entrada.
 */
public record OrderCreatedEvent(
        String orderId,
        String customerId,
        boolean simulatePaymentFailure,
        List<OrderLine> items,
        BigDecimal totalAmount) {
}