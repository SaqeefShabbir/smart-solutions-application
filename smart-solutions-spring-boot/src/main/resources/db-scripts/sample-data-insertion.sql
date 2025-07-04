-- Insert roles
INSERT INTO iot.roles (role_name, description) VALUES
('ADMIN', 'System administrator with full access'),
('OPERATOR', 'Can manage devices and view data'),
('VIEWER', 'Can only view data'),
('DEVICE', 'Special role for device authentication');

-- Insert device types
INSERT INTO iot.device_types (type_name, description, manufacturer, model, capabilities) VALUES
('Temperature Sensor', 'Measures ambient temperature', 'IoT Devices Inc.', 'TD-100', '{"measurement_range": "-40 to 125", "accuracy": "±0.5"}'),
('Humidity Sensor', 'Measures relative humidity', 'IoT Devices Inc.', 'HD-200', '{"measurement_range": "0 to 100", "accuracy": "±2%"}'),
('Smart Actuator', 'Controls connected devices', 'Smart Controls Co.', 'SA-500', '{"control_types": ["relay", "pwm"], "max_current": "10A"}');

-- Insert locations
INSERT INTO iot.locations (location_name, address, latitude, longitude, description) VALUES
('Headquarters', '123 Tech Park, Innovation City', 37.7749, -122.4194, 'Main company headquarters'),
('Warehouse A', '456 Industrial Zone', 37.7849, -122.4294, 'Primary storage warehouse'),
('Office Building', '789 Business District', 37.7949, -122.4394, 'Administrative offices');

-- Insert users
INSERT INTO iot.users (username, email, password_hash, first_name, last_name) VALUES
('admin', 'admin@smartsolutions.com', '$2a$10$xJwL5v9zZ1hBp3ZQ1fVXe.9Xq6Jw1YbZ9Xq6Jw1YbZ9Xq6Jw1YbZ9X', 'System', 'Admin'),
('operator1', 'operator1@smartsolutions.com', '$2a$10$xJwL5v9zZ1hBp3ZQ1fVXe.9Xq6Jw1YbZ9Xq6Jw1YbZ9Xq6Jw1YbZ9X', 'John', 'Operator'),
('viewer1', 'viewer1@smartsolutions.com', '$2a$10$xJwL5v9zZ1hBp3ZQ1fVXe.9Xq6Jw1YbZ9Xq6Jw1YbZ9Xq6Jw1YbZ9X', 'Sarah', 'Viewer');

-- Assign roles to users
INSERT INTO iot.user_roles (user_id, role_id) VALUES
(1, 1), -- admin has ADMIN role
(2, 2), -- operator1 has OPERATOR role
(3, 3); -- viewer1 has VIEWER role

-- Insert devices
INSERT INTO iot.devices (device_name, type_id, serial_number, mac_address, ip_address, location_id, status, is_online, firmware_version, created_by) VALUES
('Temp Sensor 1', 1, 'TS100-001', '00:1A:3F:4B:5C:6D', '192.168.1.101', 1, 'ACTIVE', TRUE, 'v1.2.3', 1),
('Humidity Sensor 1', 2, 'HS200-001', '00:1B:4C:5D:6E:7F', '192.168.1.102', 1, 'ACTIVE', TRUE, 'v2.0.1', 1),
('Warehouse Actuator', 3, 'SA500-001', '00:1C:5D:6E:7F:8A', '192.168.1.201', 2, 'ACTIVE', TRUE, 'v3.1.0', 2);

-- Insert sensor data
INSERT INTO iot.sensor_data (device_id, sensor_type, value, unit, timestamp, accuracy) VALUES
(1, 'temperature', 22.5, '°C', NOW() - INTERVAL '1 hour', 0.5),
(1, 'temperature', 23.1, '°C', NOW() - INTERVAL '30 minutes', 0.5),
(1, 'temperature', 23.7, '°C', NOW(), 0.5),
(2, 'humidity', 45.2, '%', NOW() - INTERVAL '1 hour', 2.0),
(2, 'humidity', 46.8, '%', NOW() - INTERVAL '30 minutes', 2.0),
(2, 'humidity', 47.5, '%', NOW(), 2.0);

-- Insert device alerts
INSERT INTO iot.device_alerts (device_id, alert_type, severity, message) VALUES
(1, 'HIGH_TEMP', 'MEDIUM', 'Temperature above normal threshold: 23.7°C'),
(3, 'CONNECTION_LOST', 'HIGH', 'Device not responding to ping');