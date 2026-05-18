package com.example.ecommercecamel.api;

import java.math.BigDecimal;

/**
 * Item informado durante a criacao de um pedido.
 */
public record OrderItemRequest(
        String productId,
        int quantity,
        BigDecimal unitPrice) {
}