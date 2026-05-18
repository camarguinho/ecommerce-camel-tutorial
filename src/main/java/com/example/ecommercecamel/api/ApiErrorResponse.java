package com.example.ecommercecamel.api;

/**
 * Representa o payload padrao de erro retornado pela API HTTP.
 */
public record ApiErrorResponse(
        String code,
        String message) {
}