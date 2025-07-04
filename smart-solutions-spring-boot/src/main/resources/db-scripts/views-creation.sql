-- View for device status overview
CREATE OR REPLACE VIEW iot.device_status_view AS
SELECT
    d.device_id,
    d.device_name,
    dt.type_name AS device_type,
    l.location_name,
    d.status,
    d.is_online,
    d.last_seen_at,
    COUNT(a.alert_id) FILTER (WHERE a.is_acknowledged = FALSE) AS active_alerts
FROM
    iot.devices d
JOIN
    iot.device_types dt ON d.type_id = dt.type_id
LEFT JOIN
    iot.locations l ON d.location_id = l.location_id
LEFT JOIN
    iot.device_alerts a ON d.device_id = a.device_id AND a.is_acknowledged = FALSE
GROUP BY
    d.device_id, dt.type_name, l.location_name;

-- View for latest sensor readings
CREATE OR REPLACE VIEW iot.latest_sensor_readings AS
WITH latest_readings AS (
    SELECT
        device_id,
        sensor_type,
        MAX(timestamp) AS latest_timestamp
    FROM
        iot.sensor_data
    GROUP BY
        device_id, sensor_type
)
SELECT
    sd.data_id,
    sd.device_id,
    d.device_name,
    sd.sensor_type,
    sd.value,
    sd.unit,
    sd.timestamp,
    sd.accuracy
FROM
    iot.sensor_data sd
JOIN
    latest_readings lr ON sd.device_id = lr.device_id
    AND sd.sensor_type = lr.sensor_type
    AND sd.timestamp = lr.latest_timestamp
JOIN
    iot.devices d ON sd.device_id = d.device_id;