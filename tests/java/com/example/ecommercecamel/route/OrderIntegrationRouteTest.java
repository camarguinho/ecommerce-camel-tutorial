package com.example.ecommercecamel.route;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.Test;

/**
 * Valida o fluxo de consumo Kafka e persistencia sem depender de infraestrutura externa.
 */
class OrderIntegrationRouteTest {

    @Test
    void shouldConsumeOrderCreatedAndSendToPersistenceEndpoint() throws Exception {
        try (CamelContext camelContext = new DefaultCamelContext()) {
            Properties properties = new Properties();
            properties.setProperty("order.created.consumer.endpoint", "direct:test-order-created-consumer");
            properties.setProperty("order.created.persistence.endpoint", "mock:order-persistence");
            properties.setProperty("order.created.dlt.endpoint", "mock:order-dlt");
          properties.setProperty("order.created.retry.max", "1");
          properties.setProperty("order.created.retry.delay.ms", "1");
            camelContext.getPropertiesComponent().setInitialProperties(properties);

            camelContext.addRoutes(new OrderIntegrationRoute());
            camelContext.start();

            MockEndpoint persistenceEndpoint = camelContext.getEndpoint("mock:order-persistence", MockEndpoint.class);
            persistenceEndpoint.expectedMessageCount(1);
            MockEndpoint dltEndpoint = camelContext.getEndpoint("mock:order-dlt", MockEndpoint.class);
            dltEndpoint.expectedMessageCount(0);

            ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
            producerTemplate.sendBody("direct:test-order-created-consumer", """
                    {
                      "orderId": "7cf0f3c6-3ed2-4c72-9df5-404c9df52977",
                      "customerId": "de305d54-75b4-431b-adb2-eb6b9e546014",
                      "simulatePaymentFailure": false,
                      "items": [
                        {
                          "productId": "8f95de2b-5c39-4b72-9c6a-6f793f4dc001",
                          "quantity": 1,
                          "unitPrice": 350.00
                        }
                      ],
                      "totalAmount": 350.00
                    }
                    """);

            persistenceEndpoint.assertIsSatisfied();
            dltEndpoint.assertIsSatisfied();

            Exchange exchange = persistenceEndpoint.getExchanges().get(0);
            assertThat(exchange.getIn().getHeader("orderId", String.class)).isEqualTo("7cf0f3c6-3ed2-4c72-9df5-404c9df52977");
            assertThat(exchange.getIn().getHeader("customerId", String.class)).isEqualTo("de305d54-75b4-431b-adb2-eb6b9e546014");
            assertThat(exchange.getIn().getHeader("status", String.class)).isEqualTo("RECEIVED");
            assertThat(exchange.getIn().getHeader("receivedAt", String.class)).isNotBlank();
            assertThat(exchange.getIn().getHeader("payload", String.class)).contains("\"orderId\": \"7cf0f3c6-3ed2-4c72-9df5-404c9df52977\"");
        }
    }

    @Test
    void shouldSendMessageToDeadLetterEndpointAfterRetriesExhausted() throws Exception {
        try (CamelContext camelContext = new DefaultCamelContext()) {
            Properties properties = new Properties();
            properties.setProperty("order.created.consumer.endpoint", "direct:test-order-created-consumer");
            properties.setProperty("order.created.persistence.endpoint", "mock:failing-persistence");
            properties.setProperty("order.created.dlt.endpoint", "mock:order-dlt");
            properties.setProperty("order.created.retry.max", "1");
            properties.setProperty("order.created.retry.delay.ms", "1");
            camelContext.getPropertiesComponent().setInitialProperties(properties);

            camelContext.addRoutes(new OrderIntegrationRoute());
            camelContext.start();

            MockEndpoint failingPersistenceEndpoint = camelContext.getEndpoint("mock:failing-persistence", MockEndpoint.class);
            failingPersistenceEndpoint.whenAnyExchangeReceived(exchange -> {
                throw new IllegalStateException("database unavailable");
            });
            MockEndpoint dltEndpoint = camelContext.getEndpoint("mock:order-dlt", MockEndpoint.class);
            dltEndpoint.expectedMessageCount(1);

            ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
            producerTemplate.send("direct:test-order-created-consumer", exchange -> {
                exchange.setPattern(ExchangePattern.InOnly);
                exchange.getIn().setBody("""
                        {
                          "orderId": "a39844fb-231e-42fc-b328-ac8fc3c7f523",
                          "customerId": "de305d54-75b4-431b-adb2-eb6b9e546014",
                          "simulatePaymentFailure": false,
                          "items": [
                            {
                              "productId": "8f95de2b-5c39-4b72-9c6a-6f793f4dc001",
                              "quantity": 1,
                              "unitPrice": 350.00
                            }
                          ],
                          "totalAmount": 350.00
                        }
                        """);
            });

            dltEndpoint.assertIsSatisfied();

            Exchange exchange = dltEndpoint.getExchanges().get(0);
            assertThat(exchange.getIn().getHeader("eventId", String.class)).isEqualTo("a39844fb-231e-42fc-b328-ac8fc3c7f523");
            assertThat(exchange.getIn().getHeader("failureReason", String.class)).contains("database unavailable");
            assertThat(exchange.getIn().getHeader("payload", String.class)).contains("a39844fb-231e-42fc-b328-ac8fc3c7f523");
        }
    }
}