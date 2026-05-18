package com.example.ecommercecamel;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.Test;

/**
 * Valida o endpoint interno usado pelo health check da aplicacao.
 */
class HealthRouteTest {

    @Test
    void shouldReturnServiceIndex() throws Exception {
        try (CamelContext camelContext = new DefaultCamelContext()) {
            camelContext.addRoutes(new HealthRoute());
            camelContext.start();

            ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
            String response = producerTemplate.requestBody("direct:service-index", null, String.class);

            assertThat(response).contains("\"service\":\"ecommerce-camel-tutorial\"");
            assertThat(response).contains("\"health\":\"/health\"");
            assertThat(response).contains("\"orders\":\"/api/orders\"");
        }
    }

    @Test
    void shouldReturnUpStatus() throws Exception {
        try (CamelContext camelContext = new DefaultCamelContext()) {
            camelContext.addRoutes(new HealthRoute());
            camelContext.start();

            ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
            String response = producerTemplate.requestBody("direct:health-check", null, String.class);

            assertThat(response).contains("\"status\":\"UP\"");
            assertThat(response).contains("\"service\":\"ecommerce-camel-tutorial\"");
        }
    }
}