package com.smartsolutions.smart_solutions.repository;

import com.smartsolutions.smart_solutions.model.SensorData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface SensorDataRepository extends JpaRepository<SensorData, Long> {

    Page<SensorData> findByDeviceId(Long deviceId, Pageable pageable);

    Page<SensorData> findByDeviceIdAndSensorType(Long deviceId, String sensorType, Pageable pageable);

    Page<SensorData> findByDeviceIdAndTimestampBetween(
            Long deviceId, Instant startDate, Instant endDate, Pageable pageable);

    Page<SensorData> findByDeviceIdAndSensorTypeAndTimestampBetween(
            Long deviceId, String sensorType, Instant startDate, Instant endDate, Pageable pageable);

    @Query("SELECT sd FROM SensorData sd ORDER BY sd.timestamp DESC LIMIT :count")
    List<SensorData> findLatestReadings(@Param("count") int count);

    @Query("SELECT DISTINCT sd.sensorType FROM SensorData sd WHERE sd.device.id = :deviceId")
    List<String> findDistinctSensorTypesByDeviceId(@Param("deviceId") Long deviceId);
}