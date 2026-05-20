--liquibase formatted sql

--changeset camargo:002-create-order-event-summary-view runOnChange:true
DROP VIEW IF EXISTS ecommerce.order_event_summary;

CREATE VIEW ecommerce.order_event_summary AS
SELECT
    customer_id,
    COUNT(*) AS total_orders,
    SUM(total_amount) AS total_amount,
    MAX(received_at) AS last_received_at
FROM ecommerce.order_events
GROUP BY customer_id;