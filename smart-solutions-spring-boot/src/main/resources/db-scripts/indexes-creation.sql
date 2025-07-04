-- Indexes for performance optimization
CREATE INDEX idx_sensor_data_device_id ON iot.sensor_data(device_id);
CREATE INDEX idx_sensor_data_timestamp ON iot.sensor_data(timestamp);
CREATE INDEX idx_sensor_data_sensor_type ON iot.sensor_data(sensor_type);
CREATE INDEX idx_devices_location_id ON iot.devices(location_id);
CREATE INDEX idx_devices_status ON iot.devices(status);
CREATE INDEX idx_devices_is_online ON iot.devices(is_online);
CREATE INDEX idx_device_commands_device_id ON iot.device_commands(device_id);
CREATE INDEX idx_device_commands_status ON iot.device_commands(status);
CREATE INDEX idx_audit_log_entity ON iot.audit_log(entity_type, entity_id);

-- Create indexes for frequently queried columns
CREATE INDEX idx_device_alerts_device_id ON iot.device_alerts(device_id);
CREATE INDEX idx_device_alerts_status ON iot.device_alerts(status);
CREATE INDEX idx_device_alerts_created_at ON iot.device_alerts(created_at);
CREATE INDEX idx_device_alerts_severity ON iot.device_alerts(severity);
