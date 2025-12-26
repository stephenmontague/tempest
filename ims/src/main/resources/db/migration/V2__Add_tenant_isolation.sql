-- V2: Add tenant isolation and audit columns to all tables
-- This migration adds multi-tenancy support with tenant_id column
-- and audit columns for tracking who created/modified records

-- Create items table with tenant isolation
CREATE TABLE IF NOT EXISTS items (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    sku VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT true,
    created_by_user_id VARCHAR(255),
    updated_by_user_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    -- Tenant-scoped unique constraint for SKU
    CONSTRAINT uk_items_tenant_sku UNIQUE (tenant_id, sku)
);

-- Index for tenant queries
CREATE INDEX IF NOT EXISTS idx_items_tenant_id ON items(tenant_id);

-- Comment on columns for documentation
COMMENT ON COLUMN items.tenant_id IS 'Tenant identifier - derived from JWT tenant_id claim';
COMMENT ON COLUMN items.created_by_user_id IS 'User who created this record - from JWT sub claim';
COMMENT ON COLUMN items.updated_by_user_id IS 'User who last updated this record - from JWT sub claim';

