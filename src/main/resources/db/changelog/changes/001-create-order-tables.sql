--liquibase formatted sql

--changeset camargo:001-create-order-events-table
CREATE TABLE IF NOT EXISTS order_events (
    id BIGSERIAL PRIMARY KEY,
    order_id UUID NOT NULL UNIQUE,
    customer_id UUID NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL,
    payload JSONB NOT NULL
);

--changeset camargo:001-create-order-events-index
CREATE INDEX IF NOT EXISTS idx_order_events_customer_id ON order_events (customer_id);

--changeset camargo:001-create-order-event-dlt-table
CREATE TABLE IF NOT EXISTS order_event_dlt (
    id BIGSERIAL PRIMARY KEY,
    event_id UUID NOT NULL UNIQUE,
    failed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    failure_reason TEXT NOT NULL,
    payload JSONB NOT NULL
);