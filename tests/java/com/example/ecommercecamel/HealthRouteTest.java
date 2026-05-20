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
            assertThat(response).contains("\"h2Console\":\"/h2-console\"");
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

    @Test
    void shouldRedirectToStandaloneH2Console() throws Exception {
        try (CamelContext camelContext = new DefaultCamelContext()) {
            camelContext.addRoutes(new HealthRoute());
            camelContext.start();

            ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
            var exchange = producerTemplate.request("direct:h2-console-redirect", request -> {
                request.getMessage().setHeader("Host", "localhost:8080");
            });

            assertThat(exchange.getMessage().getHeader("Location", String.class)).isEqualTo("http://localhost:8082/");
            assertThat(exchange.getMessage().getHeader(org.apache.camel.Exchange.HTTP_RESPONSE_CODE, Integer.class)).isEqualTo(302);
        }
    }
}