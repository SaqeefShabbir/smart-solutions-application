-- Migration script for creating the device_alerts table in iot schema
-- Includes all fields from the Alert entity class with proper constraints

BEGIN;

-- Create the table with all columns
CREATE TABLE iot.device_alerts (
    alert_id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    additional_data JSONB,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    is_active BOOLEAN GENERATED ALWAYS AS (
        CASE WHEN status IN ('OPEN', 'ACKNOWLEDGED') THEN TRUE ELSE FALSE END
    ) STORED,
    created_by BIGINT,
    acknowledged_by BIGINT,
    resolved_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    acknowledged_at TIMESTAMPTZ,
    resolved_at TIMESTAMPTZ
);

-- Add constraints
ALTER TABLE iot.device_alerts
    ADD CONSTRAINT fk_device_alerts_device
    FOREIGN KEY (device_id) REFERENCES iot.devices(device_id) ON DELETE CASCADE;

ALTER TABLE iot.device_alerts
    ADD CONSTRAINT fk_device_alerts_created_by
    FOREIGN KEY (created_by) REFERENCES iot.users(user_id);

ALTER TABLE iot.device_alerts
    ADD CONSTRAINT fk_device_alerts_acknowledged_by
    FOREIGN KEY (acknowledged_by) REFERENCES iot.users(user_id);

ALTER TABLE iot.device_alerts
    ADD CONSTRAINT fk_device_alerts_resolved_by
    FOREIGN KEY (resolved_by) REFERENCES iot.users(user_id);

ALTER TABLE iot.device_alerts
    ADD CONSTRAINT chk_device_alerts_severity
    CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'));

ALTER TABLE iot.device_alerts
    ADD CONSTRAINT chk_device_alerts_status
    CHECK (status IN ('OPEN', 'ACKNOWLEDGED', 'RESOLVED', 'SUPPRESSED', 'UNACKNOWLEDGED'));

-- Create indexes
CREATE INDEX idx_device_alerts_device_id ON iot.device_alerts(device_id);
CREATE INDEX idx_device_alerts_status ON iot.device_alerts(status);
CREATE INDEX idx_device_alerts_created_at ON iot.device_alerts(created_at);
CREATE INDEX idx_device_alerts_severity ON iot.device_alerts(severity);
CREATE INDEX idx_device_alerts_is_active ON iot.device_alerts(is_active);

-- Add comments for documentation
COMMENT ON TABLE iot.device_alerts IS 'Stores alert information for IoT devices';
COMMENT ON COLUMN iot.device_alerts.alert_id IS 'Primary key identifier';
COMMENT ON COLUMN iot.device_alerts.device_id IS 'Reference to the device that generated the alert';
COMMENT ON COLUMN iot.device_alerts.status IS 'Current status of the alert (OPEN, ACKNOWLEDGED, RESOLVED, etc.)';
COMMENT ON COLUMN iot.device_alerts.is_active IS 'Automatically computed field indicating if alert is active';

-- Create trigger for updated_at timestamp
CREATE OR REPLACE FUNCTION update_device_alerts_modified_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_device_alerts_update
BEFORE UPDATE ON iot.device_alerts
FOR EACH ROW
EXECUTE FUNCTION update_device_alerts_modified_at();

COMMIT;