-- Create a function for sensor data partitioning
CREATE OR REPLACE FUNCTION iot.create_sensor_data_partition(partition_date DATE)
RETURNS VOID AS $$
DECLARE
    partition_name TEXT;
    partition_start TEXT;
    partition_end TEXT;
BEGIN
    partition_name := 'sensor_data_' || TO_CHAR(partition_date, 'YYYY_MM');
    partition_start := TO_CHAR(partition_date, 'YYYY-MM-01');
    partition_end := TO_CHAR(partition_date + INTERVAL '1 month', 'YYYY-MM-01');

    EXECUTE format(
        'CREATE TABLE IF NOT EXISTS iot.%I PARTITION OF iot.sensor_data
        FOR VALUES FROM (%L) TO (%L)',
        partition_name, partition_start, partition_end
    );
END;
$$ LANGUAGE plpgsql;

-- Create a function for archiving old data
CREATE OR REPLACE FUNCTION iot.archive_old_sensor_data(archive_months INTEGER)
RETURNS INTEGER AS $$
DECLARE
    archive_cutoff TIMESTAMP;
    rows_archived INTEGER;
BEGIN
    archive_cutoff := NOW() - (archive_months * INTERVAL '1 month');

    EXECUTE format(
        'WITH archived AS (
            DELETE FROM iot.sensor_data
            WHERE timestamp < %L
            RETURNING *
        )
        INSERT INTO iot.sensor_data_archive
        SELECT * FROM archived',
        archive_cutoff
    );

    GET DIAGNOSTICS rows_archived = ROW_COUNT;
    RETURN rows_archived;
END;
$$ LANGUAGE plpgsql;