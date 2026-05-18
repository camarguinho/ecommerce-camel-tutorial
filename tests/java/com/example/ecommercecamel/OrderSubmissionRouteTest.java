package com.example.ecommercecamel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.ecommercecamel.api.CreateOrderRequest;
import com.example.ecommercecamel.api.OrderAcceptedResponse;
import com.example.ecommercecamel.api.OrderItemRequest;
import com.example.ecommercecamel.domain.OrderCreatedEvent;
import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.Test;

/**
 * Garante o comportamento principal da rota de submissao de pedidos.
 */
class OrderSubmissionRouteTest {

    @Test
    void shouldCreateOrderEventAndAcceptedResponse() throws Exception {
        try (CamelContext camelContext = new DefaultCamelContext()) {
            Properties properties = new Properties();
            properties.setProperty("order.created.endpoint", "mock:order-created");
            camelContext.getPropertiesComponent().setInitialProperties(properties);

            camelContext.addRoutes(new OrderSubmissionRoute());
            camelContext.addRoutes(new OrderCreatedEventRoute());
            camelContext.start();

            ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
            MockEndpoint mockEndpoint = camelContext.getEndpoint("mock:order-created", MockEndpoint.class);
            mockEndpoint.expectedMessageCount(1);
            CreateOrderRequest request = new CreateOrderRequest(
                    "de305d54-75b4-431b-adb2-eb6b9e546014",
                    false,
                    List.of(new OrderItemRequest(
                            "8f95de2b-5c39-4b72-9c6a-6f793f4dc001",
                            2,
                            new BigDecimal("350.00"))));

            Exchange exchange = producerTemplate.request("direct:submit-order", incomingExchange ->
                    incomingExchange.getMessage().setBody(request));

            OrderAcceptedResponse response = exchange.getMessage().getBody(OrderAcceptedResponse.class);
            OrderCreatedEvent event = exchange.getProperty("orderCreatedEvent", OrderCreatedEvent.class);

            mockEndpoint.assertIsSatisfied();

            assertThat(response.status()).isEqualTo("RECEIVED");
            assertThat(response.totalAmount()).isEqualByComparingTo("700.00");
            assertThat(response.orderId()).isNotBlank();

            assertThat(event).isNotNull();
            assertThat(event.orderId()).isEqualTo(response.orderId());
            assertThat(event.customerId()).isEqualTo(request.customerId());
            assertThat(event.totalAmount()).isEqualByComparingTo("700.00");
            assertThat(event.items()).hasSize(1);
            assertThat(event.items().get(0).productId()).isEqualTo("8f95de2b-5c39-4b72-9c6a-6f793f4dc001");
            assertThat(event.items().get(0).quantity()).isEqualTo(2);
            assertThat(event.items().get(0).unitPrice()).isEqualByComparingTo("350.00");
            assertThat(mockEndpoint.getExchanges().get(0).getIn().getHeader("kafka.KEY", String.class))
                    .isEqualTo(response.orderId());
            assertThat(mockEndpoint.getExchanges().get(0).getIn().getBody(String.class))
                    .contains(response.orderId())
                    .contains("\"customerId\":\"de305d54-75b4-431b-adb2-eb6b9e546014\"");
        }
    }

    @Test
    void shouldRejectRequestWithoutCustomerId() throws Exception {
        try (CamelContext camelContext = new DefaultCamelContext()) {
            Properties properties = new Properties();
            properties.setProperty("order.created.endpoint", "mock:order-created");
            camelContext.getPropertiesComponent().setInitialProperties(properties);

            camelContext.addRoutes(new OrderSubmissionRoute());
            camelContext.addRoutes(new OrderCreatedEventRoute());
            camelContext.start();

            ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
            CreateOrderRequest request = new CreateOrderRequest(
                    "",
                    false,
                    List.of(new OrderItemRequest(
                            "8f95de2b-5c39-4b72-9c6a-6f793f4dc001",
                            1,
                            new BigDecimal("350.00"))));

            assertThatThrownBy(() -> producerTemplate.requestBody("direct:submit-order", request, OrderAcceptedResponse.class))
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .rootCause()
                    .hasMessage("customerId is required");
        }
    }

    @Test
    void shouldRejectRequestWithoutItems() throws Exception {
        try (CamelContext camelContext = new DefaultCamelContext()) {
            Properties properties = new Properties();
            properties.setProperty("order.created.endpoint", "mock:order-created");
            camelContext.getPropertiesComponent().setInitialProperties(properties);

            camelContext.addRoutes(new OrderSubmissionRoute());
            camelContext.addRoutes(new OrderCreatedEventRoute());
            camelContext.start();

            ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
            CreateOrderRequest request = new CreateOrderRequest(
                    "de305d54-75b4-431b-adb2-eb6b9e546014",
                    false,
                    List.of());

            assertThatThrownBy(() -> producerTemplate.requestBody("direct:submit-order", request, OrderAcceptedResponse.class))
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .rootCause()
                    .hasMessage("At least one item is required");
        }
    }
}