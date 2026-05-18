package com.example.ecommercecamel.domain;

import java.math.BigDecimal;

/**
 * Snapshot de item transportado no evento interno do pedido.
 */
public record OrderLine(
        String productId,
        int quantity,
        BigDecimal unitPrice) {
}