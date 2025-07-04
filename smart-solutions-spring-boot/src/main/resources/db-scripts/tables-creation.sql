-- Users table for authentication
CREATE TABLE iot.users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP WITH TIME ZONE
);

-- Roles table
CREATE TABLE iot.roles (
    role_id SERIAL PRIMARY KEY,
    role_name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT
);

-- User roles junction table
CREATE TABLE iot.user_roles (
    user_id INTEGER REFERENCES iot.users(user_id) ON DELETE CASCADE,
    role_id INTEGER REFERENCES iot.roles(role_id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Device types table
CREATE TABLE iot.device_types (
    type_id SERIAL PRIMARY KEY,
    type_name VARCHAR(100) NOT NULL,
    description TEXT,
    manufacturer VARCHAR(100),
    model VARCHAR(100),
    capabilities JSONB
);

-- Devices table
CREATE TABLE iot.devices (
    device_id SERIAL PRIMARY KEY,
    device_name VARCHAR(100) NOT NULL,
    type_id INTEGER REFERENCES iot.device_types(type_id),
    serial_number VARCHAR(100) UNIQUE,
    mac_address VARCHAR(17) UNIQUE,
    ip_address VARCHAR(15),
    location_id INTEGER,
    status VARCHAR(20) CHECK (status IN ('ACTIVE', 'INACTIVE', 'MAINTENANCE', 'RETIRED')),
    is_online BOOLEAN DEFAULT FALSE,
    last_seen_at TIMESTAMP WITH TIME ZONE,
    firmware_version VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER REFERENCES iot.users(user_id),
    metadata JSONB
);

-- Locations table
CREATE TABLE iot.locations (
    location_id SERIAL PRIMARY KEY,
    location_name VARCHAR(100) NOT NULL,
    parent_location_id INTEGER REFERENCES iot.locations(location_id),
    address TEXT,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    description TEXT
);

-- Sensor data table
CREATE TABLE iot.sensor_data (
    data_id BIGSERIAL PRIMARY KEY,
    device_id INTEGER REFERENCES iot.devices(device_id) ON DELETE CASCADE,
    sensor_type VARCHAR(50) NOT NULL,
    value DOUBLE PRECISION NOT NULL,
    unit VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    accuracy DECIMAL(5, 2),
    status_code INTEGER,
    metadata JSONB
);

-- Device alerts table
CREATE TABLE iot.device_alerts (
    alert_id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL REFERENCES iot.devices(device_id) ON DELETE CASCADE,
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    message TEXT NOT NULL,
    additional_data JSONB,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN'
        CHECK (status IN ('OPEN', 'ACKNOWLEDGED', 'RESOLVED', 'SUPPRESSED', 'UNACKNOWLEDGED')),
    is_active BOOLEAN GENERATED ALWAYS AS (
        CASE WHEN status IN ('OPEN', 'ACKNOWLEDGED') THEN TRUE ELSE FALSE END
    ) STORED,
    created_by BIGINT REFERENCES iot.users(user_id),
    acknowledged_by BIGINT REFERENCES iot.users(user_id),
    resolved_by BIGINT REFERENCES iot.users(user_id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    acknowledged_at TIMESTAMPTZ,
    resolved_at TIMESTAMPTZ,
);


-- Device commands table
CREATE TABLE iot.device_commands (
    command_id SERIAL PRIMARY KEY,
    device_id INTEGER REFERENCES iot.devices(device_id) ON DELETE CASCADE,
    command_name VARCHAR(100) NOT NULL,
    parameters JSONB,
    issued_by INTEGER REFERENCES iot.users(user_id),
    issued_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) CHECK (status IN ('PENDING', 'SENT', 'COMPLETED', 'FAILED')),
    completed_at TIMESTAMP WITH TIME ZONE,
    response TEXT
);

-- Audit log table
CREATE TABLE iot.audit_log (
    log_id BIGSERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES iot.users(user_id),
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id INTEGER,
    old_value JSONB,
    new_value JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Notification settings table
CREATE TABLE iot.user_notification_settings (
    notification_id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES iot.users(user_id) ON DELETE CASCADE,
    email_alerts BOOLEAN DEFAULT TRUE,
    push_notifications BOOLEAN DEFAULT TRUE,
    sms_alerts BOOLEAN DEFAULT FALSE,
    critical_only BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User preferences table
CREATE TABLE iot.user_preferences (
    preference_id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES iot.users(user_id) ON DELETE CASCADE,
    theme VARCHAR(20) DEFAULT 'light',
    language VARCHAR(10) DEFAULT 'en',
    timezone VARCHAR(50) DEFAULT 'UTC',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);