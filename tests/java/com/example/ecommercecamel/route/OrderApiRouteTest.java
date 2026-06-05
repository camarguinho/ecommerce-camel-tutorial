package com.example.ecommercecamel.route;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;
import org.apache.camel.CamelContext;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.Test;

/**
 * Exercita a adaptacao JSON do boundary HTTP sem depender do transporte Vert.x.
 */
class OrderApiRouteTest {

    @Test
    void shouldReturnAcceptedResponseForValidJson() throws Exception {
        try (CamelContext camelContext = new DefaultCamelContext()) {
            Properties properties = new Properties();
            properties.setProperty("order.created.endpoint", "mock:order-created");
            camelContext.getPropertiesComponent().setInitialProperties(properties);

            camelContext.addRoutes(new OrderApiRoute());
            camelContext.addRoutes(new OrderSubmissionRoute());
            camelContext.addRoutes(new OrderCreatedEventRoute());
            camelContext.start();

            ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
            MockEndpoint mockEndpoint = camelContext.getEndpoint("mock:order-created", MockEndpoint.class);
            mockEndpoint.expectedMessageCount(1);
            String response = producerTemplate.requestBody("direct:submit-order-rest", """
                    {
                      "customerId": "de305d54-75b4-431b-adb2-eb6b9e546014",
                      "simulatePaymentFailure": false,
                      "items": [
                        {
                          "productId": "8f95de2b-5c39-4b72-9c6a-6f793f4dc001",
                          "quantity": 1,
                          "unitPrice": 350.00
                        }
                      ]
                    }
                    """, String.class);

                mockEndpoint.assertIsSatisfied();

            assertThat(response).contains("\"status\":\"RECEIVED\"");
            assertThat(response).contains("\"totalAmount\":350.00");
        }
    }

    @Test
    void shouldReturnValidationErrorForInvalidPayload() throws Exception {
        try (CamelContext camelContext = new DefaultCamelContext()) {
        Properties properties = new Properties();
        properties.setProperty("order.created.endpoint", "mock:order-created");
        camelContext.getPropertiesComponent().setInitialProperties(properties);

            camelContext.addRoutes(new OrderApiRoute());
            camelContext.addRoutes(new OrderSubmissionRoute());
        camelContext.addRoutes(new OrderCreatedEventRoute());
            camelContext.start();

            ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
            String response = producerTemplate.requestBody("direct:submit-order-rest", """
                    {
                      "customerId": "",
                      "simulatePaymentFailure": false,
                      "items": []
                    }
                    """, String.class);

            assertThat(response).contains("\"code\":\"INVALID_REQUEST\"");
            assertThat(response).contains("customerId is required");
        }
    }

    @Test
    void shouldReturnJsonErrorForMalformedJson() throws Exception {
        try (CamelContext camelContext = new DefaultCamelContext()) {
        Properties properties = new Properties();
        properties.setProperty("order.created.endpoint", "mock:order-created");
        camelContext.getPropertiesComponent().setInitialProperties(properties);

            camelContext.addRoutes(new OrderApiRoute());
            camelContext.addRoutes(new OrderSubmissionRoute());
        camelContext.addRoutes(new OrderCreatedEventRoute());
            camelContext.start();

            ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
            String response = producerTemplate.requestBody("direct:submit-order-rest", "{ invalid-json }", String.class);

            assertThat(response).contains("\"code\":\"INVALID_JSON\"");
            assertThat(response).contains("Request body must contain valid JSON");
        }
    }
}