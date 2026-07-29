-- V3: Add demo facility for local development
-- This creates a default warehouse facility for the demo tenant

INSERT INTO facilities (
    tenant_id,
    code,
    name,
    facility_type,
    address_line1,
    city,
    state,
    postal_code,
    country,
    active,
    created_at,
    updated_at
) VALUES (
    'demo-tenant',
    'WH-001',
    'Main Warehouse',
    'WAREHOUSE',
    '123 Warehouse Lane',
    'Austin',
    'TX',
    '78701',
    'USA',
    true,
    NOW(),
    NOW()
) ON CONFLICT (tenant_id, code) DO NOTHING;

