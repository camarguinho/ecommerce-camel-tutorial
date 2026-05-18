package com.example.ecommercecamel.api;

import java.math.BigDecimal;

/**
 * Retorno sintetico entregue ao cliente logo apos a submissao do pedido.
 */
public record OrderAcceptedResponse(
        String orderId,
        String status,
        BigDecimal totalAmount) {
}