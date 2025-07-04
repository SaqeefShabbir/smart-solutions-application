-- Add foreign key for devices.location_id that was created earlier
ALTER TABLE iot.devices
ADD CONSTRAINT fk_devices_location
FOREIGN KEY (location_id) REFERENCES iot.locations(location_id);

-- Indexes for better query performance
CONSTRAINT fk_device FOREIGN KEY (device_id) REFERENCES iot.devices(device_id),
CONSTRAINT fk_created_by FOREIGN KEY (created_by) REFERENCES iot.users(user_id),
CONSTRAINT fk_acknowledged_by FOREIGN KEY (acknowledged_by) REFERENCES iot.users(user_id),
CONSTRAINT fk_resolved_by FOREIGN KEY (resolved_by) REFERENCES iot.users(user_id)

ALTER TABLE iot.users
ADD CONSTRAINT fk_user_notification_settings
FOREIGN KEY (location_id) REFERENCES iot.locations(location_id);