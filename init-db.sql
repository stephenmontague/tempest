-- =============================================================================
-- Tempest WMS - Database Initialization
-- =============================================================================
-- Creates separate databases for each microservice.
-- This script runs automatically when PostgreSQL container starts.
-- =============================================================================

-- Create databases for each service
CREATE DATABASE tempest_ims;
CREATE DATABASE tempest_oms;
CREATE DATABASE tempest_wms;
CREATE DATABASE tempest_sms;

-- Grant all privileges to the tempest user
GRANT ALL PRIVILEGES ON DATABASE tempest_ims TO tempest;
GRANT ALL PRIVILEGES ON DATABASE tempest_oms TO tempest;
GRANT ALL PRIVILEGES ON DATABASE tempest_wms TO tempest;
GRANT ALL PRIVILEGES ON DATABASE tempest_sms TO tempest;

