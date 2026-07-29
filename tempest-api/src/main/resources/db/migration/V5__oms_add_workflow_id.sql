-- V2: Add workflow_id column to orders table
-- Links orders to their Temporal workflow for status tracking

ALTER TABLE orders ADD COLUMN workflow_id VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_orders_workflow_id ON orders(workflow_id);

COMMENT ON COLUMN orders.workflow_id IS 'Temporal workflow ID for order processing';

