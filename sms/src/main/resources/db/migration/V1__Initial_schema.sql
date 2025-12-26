-- V1: Initial SMS schema with tenant isolation
-- All tables include tenant_id for multi-tenancy support

-- Shipments table
CREATE TABLE IF NOT EXISTS shipments (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    order_id BIGINT NOT NULL,
    facility_id BIGINT NOT NULL,
    carrier VARCHAR(100) NOT NULL,
    service_level VARCHAR(100) NOT NULL,
    tracking_number VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    label_url VARCHAR(1000),
    shipped_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    created_by_user_id VARCHAR(255),
    updated_by_user_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Indexes for shipments
CREATE INDEX IF NOT EXISTS idx_shipments_tenant_id ON shipments(tenant_id);
CREATE INDEX IF NOT EXISTS idx_shipments_order_id ON shipments(order_id);
CREATE INDEX IF NOT EXISTS idx_shipments_tracking ON shipments(tracking_number);
CREATE INDEX IF NOT EXISTS idx_shipments_status ON shipments(tenant_id, status);

-- Parcels table (individual packages within a shipment)
CREATE TABLE IF NOT EXISTS parcels (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    shipment_id BIGINT NOT NULL REFERENCES shipments(id),
    tracking_number VARCHAR(255),
    weight_oz DECIMAL(10, 2),
    length_in DECIMAL(10, 2),
    width_in DECIMAL(10, 2),
    height_in DECIMAL(10, 2),
    created_by_user_id VARCHAR(255),
    updated_by_user_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Indexes for parcels
CREATE INDEX IF NOT EXISTS idx_parcels_tenant_id ON parcels(tenant_id);
CREATE INDEX IF NOT EXISTS idx_parcels_shipment_id ON parcels(shipment_id);

-- Tracking events table (append-only for tracking updates)
CREATE TABLE IF NOT EXISTS tracking_events (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    shipment_id BIGINT NOT NULL REFERENCES shipments(id),
    event_type VARCHAR(100) NOT NULL,
    event_description TEXT,
    location VARCHAR(255),
    event_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Indexes for tracking events
CREATE INDEX IF NOT EXISTS idx_tracking_events_tenant_id ON tracking_events(tenant_id);
CREATE INDEX IF NOT EXISTS idx_tracking_events_shipment_id ON tracking_events(shipment_id);
CREATE INDEX IF NOT EXISTS idx_tracking_events_timestamp ON tracking_events(event_timestamp);

-- Comments
COMMENT ON COLUMN shipments.tenant_id IS 'Tenant identifier - derived from JWT tenant_id claim';
COMMENT ON COLUMN shipments.status IS 'CREATED, LABEL_GENERATED, SHIPPED, IN_TRANSIT, DELIVERED, EXCEPTION';
COMMENT ON TABLE tracking_events IS 'Append-only event log for shipment tracking updates';

