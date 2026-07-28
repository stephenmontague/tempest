-- V2: Add wave_orders join table for wave-order relationships
-- This table tracks which orders are included in each wave
CREATE TABLE IF NOT EXISTS wave_orders (
     wave_id BIGINT NOT NULL REFERENCES waves(id) ON DELETE CASCADE,
     order_id BIGINT NOT NULL,
     PRIMARY KEY (wave_id, order_id)
);
-- Index for looking up waves by order
CREATE INDEX IF NOT EXISTS idx_wave_orders_order_id ON wave_orders(order_id);
-- Add workflow_id column to waves table for tracking the Temporal workflow
ALTER TABLE waves
ADD COLUMN IF NOT EXISTS workflow_id VARCHAR(255);
COMMENT ON TABLE wave_orders IS 'Join table tracking which orders are included in each wave';
COMMENT ON COLUMN waves.workflow_id IS 'Temporal workflow ID for the wave execution workflow';