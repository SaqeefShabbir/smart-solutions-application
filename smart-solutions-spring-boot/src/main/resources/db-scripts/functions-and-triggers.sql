-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION iot.update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply trigger to devices table
CREATE TRIGGER trg_devices_update
BEFORE UPDATE ON iot.devices
FOR EACH ROW
EXECUTE FUNCTION iot.update_timestamp();

-- Function for device status change logging
CREATE OR REPLACE FUNCTION iot.log_device_status_change()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.status IS DISTINCT FROM NEW.status THEN
        INSERT INTO iot.audit_log (
            user_id,
            action,
            entity_type,
            entity_id,
            old_value,
            new_value
        ) VALUES (
            NEW.updated_by,
            'STATUS_CHANGE',
            'DEVICE',
            NEW.device_id,
            jsonb_build_object('status', OLD.status),
            jsonb_build_object('status', NEW.status)
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply trigger to devices table
CREATE TRIGGER trg_devices_status_change
AFTER UPDATE ON iot.devices
FOR EACH ROW
EXECUTE FUNCTION iot.log_device_status_change();