-- V1: Initial WMS schema with tenant isolation
-- All tables include tenant_id for multi-tenancy support

-- Facilities table
CREATE TABLE IF NOT EXISTS facilities (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    facility_type VARCHAR(50) NOT NULL DEFAULT 'WAREHOUSE',
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT true,
    created_by_user_id VARCHAR(255),
    updated_by_user_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    -- Tenant-scoped unique constraint for facility code
    CONSTRAINT uk_facilities_tenant_code UNIQUE (tenant_id, code)
);

-- Indexes for facilities
CREATE INDEX IF NOT EXISTS idx_facilities_tenant_id ON facilities(tenant_id);

-- Locations table (bins/shelves within a facility)
CREATE TABLE IF NOT EXISTS locations (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    facility_id BIGINT NOT NULL REFERENCES facilities(id),
    code VARCHAR(50) NOT NULL,
    location_type VARCHAR(50) NOT NULL DEFAULT 'PICK',
    zone VARCHAR(50),
    active BOOLEAN NOT NULL DEFAULT true,
    created_by_user_id VARCHAR(255),
    updated_by_user_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    -- Tenant-scoped unique constraint for location code within facility
    CONSTRAINT uk_locations_tenant_facility_code UNIQUE (tenant_id, facility_id, code)
);

-- Indexes for locations
CREATE INDEX IF NOT EXISTS idx_locations_tenant_id ON locations(tenant_id);
CREATE INDEX IF NOT EXISTS idx_locations_facility_id ON locations(facility_id);

-- Waves table (batch of pick tasks)
CREATE TABLE IF NOT EXISTS waves (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    facility_id BIGINT NOT NULL REFERENCES facilities(id),
    wave_number VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    created_by_user_id VARCHAR(255),
    updated_by_user_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    -- Tenant-scoped unique constraint for wave number
    CONSTRAINT uk_waves_tenant_number UNIQUE (tenant_id, wave_number)
);

-- Indexes for waves
CREATE INDEX IF NOT EXISTS idx_waves_tenant_id ON waves(tenant_id);
CREATE INDEX IF NOT EXISTS idx_waves_facility_id ON waves(facility_id);
CREATE INDEX IF NOT EXISTS idx_waves_status ON waves(tenant_id, status);

-- Pick tasks table
CREATE TABLE IF NOT EXISTS pick_tasks (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    wave_id BIGINT REFERENCES waves(id),
    order_id BIGINT NOT NULL,
    order_line_id BIGINT NOT NULL,
    sku VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    from_location_id BIGINT REFERENCES locations(id),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    assigned_user_id VARCHAR(255),
    picked_at TIMESTAMP WITH TIME ZONE,
    created_by_user_id VARCHAR(255),
    updated_by_user_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Indexes for pick tasks
CREATE INDEX IF NOT EXISTS idx_pick_tasks_tenant_id ON pick_tasks(tenant_id);
CREATE INDEX IF NOT EXISTS idx_pick_tasks_wave_id ON pick_tasks(wave_id);
CREATE INDEX IF NOT EXISTS idx_pick_tasks_status ON pick_tasks(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_pick_tasks_assigned_user ON pick_tasks(assigned_user_id);

-- Comments
COMMENT ON COLUMN facilities.tenant_id IS 'Tenant identifier - derived from JWT tenant_id claim';
COMMENT ON COLUMN facilities.facility_type IS 'WAREHOUSE or STORE';
COMMENT ON COLUMN locations.location_type IS 'PICK, BULK, STAGING, etc.';
COMMENT ON COLUMN waves.status IS 'CREATED, IN_PROGRESS, COMPLETED, CANCELLED';
COMMENT ON COLUMN pick_tasks.status IS 'PENDING, IN_PROGRESS, COMPLETED, CANCELLED';

