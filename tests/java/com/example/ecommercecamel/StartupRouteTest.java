package com.example.ecommercecamel;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.Test;

/**
 * Valida a rota minima usada para confirmar o bootstrap do projeto Camel.
 */
class StartupRouteTest {

    /**
     * Garante que a rota basica responde com a mensagem esperada.
     *
     * @throws Exception quando ocorre falha ao iniciar ou encerrar o contexto Camel.
     */
    @Test
    void shouldReturnStartupMarker() throws Exception {
        try (CamelContext camelContext = new DefaultCamelContext()) {
            RouteBuilder routeBuilder = new StartupRoute();
            camelContext.addRoutes(routeBuilder);
            camelContext.start();

            ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
            String response = producerTemplate.requestBody("direct:startup-check", null, String.class);

            assertThat(response).isEqualTo("camel-ready");
        }
    }
}