package com.example.ecommercecamel.route;

import com.example.ecommercecamel.api.ApiErrorResponse;
import com.example.ecommercecamel.api.CreateOrderRequest;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.TryDefinition;

/**
 * Adapta o boundary HTTP para o fluxo interno, incluindo validacao de JSON e mapeamento de erros.
 */
public class OrderApiRoute extends RouteBuilder {

    @Override
    public void configure() {
        TryDefinition tryDefinition = from("direct:submit-order-rest")
            .routeId("submit-order-rest-route")
            .doTry();

        tryDefinition.unmarshal().json(CreateOrderRequest.class);
        tryDefinition.to("direct:submit-order");
        tryDefinition.marshal().json();

        tryDefinition.doCatch(IllegalArgumentException.class)
            .setHeader("CamelHttpResponseCode", constant(400))
            .setHeader("Content-Type", constant("application/json"))
            .process(exchange -> exchange.getMessage().setBody(new ApiErrorResponse(
                "INVALID_REQUEST",
                exchange.getProperty(org.apache.camel.Exchange.EXCEPTION_CAUGHT, Exception.class).getMessage())))
            .marshal().json();

        tryDefinition.doCatch(com.fasterxml.jackson.core.JsonParseException.class,
                com.fasterxml.jackson.databind.JsonMappingException.class)
            .setHeader("CamelHttpResponseCode", constant(400))
            .setHeader("Content-Type", constant("application/json"))
            .setBody(constant(new ApiErrorResponse(
                "INVALID_JSON",
                "Request body must contain valid JSON")))
            .marshal().json();

        tryDefinition.doCatch(Exception.class)
            .setHeader("CamelHttpResponseCode", constant(500))
            .setHeader("Content-Type", constant("application/json"))
            .setBody(constant(new ApiErrorResponse(
                "INTERNAL_ERROR",
                "Unexpected error while processing the request")))
            .marshal().json();

        tryDefinition.end();
    }
}