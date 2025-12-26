-- V1: Initial OMS schema with tenant isolation
-- All tables include tenant_id for multi-tenancy support

-- Orders table
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    external_order_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    customer_email VARCHAR(255),
    customer_name VARCHAR(255),
    shipping_address_line1 VARCHAR(255),
    shipping_address_line2 VARCHAR(255),
    shipping_city VARCHAR(100),
    shipping_state VARCHAR(100),
    shipping_postal_code VARCHAR(20),
    shipping_country VARCHAR(100),
    created_by_user_id VARCHAR(255),
    updated_by_user_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    -- Tenant-scoped unique constraint for external order ID
    CONSTRAINT uk_orders_tenant_external_id UNIQUE (tenant_id, external_order_id)
);

-- Indexes for orders
CREATE INDEX IF NOT EXISTS idx_orders_tenant_id ON orders(tenant_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(tenant_id, status);

-- Order lines table
CREATE TABLE IF NOT EXISTS order_lines (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    sku VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10, 2),
    created_by_user_id VARCHAR(255),
    updated_by_user_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Indexes for order lines
CREATE INDEX IF NOT EXISTS idx_order_lines_tenant_id ON order_lines(tenant_id);
CREATE INDEX IF NOT EXISTS idx_order_lines_order_id ON order_lines(order_id);

-- Order events table (append-only for audit)
CREATE TABLE IF NOT EXISTS order_events (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    event_type VARCHAR(100) NOT NULL,
    event_data JSONB,
    actor_user_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Indexes for order events
CREATE INDEX IF NOT EXISTS idx_order_events_tenant_id ON order_events(tenant_id);
CREATE INDEX IF NOT EXISTS idx_order_events_order_id ON order_events(order_id);
CREATE INDEX IF NOT EXISTS idx_order_events_created_at ON order_events(created_at);

-- Comments
COMMENT ON COLUMN orders.tenant_id IS 'Tenant identifier - derived from JWT tenant_id claim';
COMMENT ON COLUMN orders.external_order_id IS 'External order ID from source system - unique per tenant';
COMMENT ON TABLE order_events IS 'Append-only event log for order lifecycle tracking';

