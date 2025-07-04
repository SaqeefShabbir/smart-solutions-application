package com.smartsolutions.smart_solutions.repository;

import com.smartsolutions.smart_solutions.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long>, JpaSpecificationExecutor<Device> {

    Optional<Device> findBySerialNumber(String serialNumber);

    Optional<Device> findByMacAddress(String macAddress);

    List<Device> findByStatus(Device.DeviceStatus status);

    List<Device> findByIsOnlineTrue();

    @Query("SELECT d FROM Device d WHERE d.lastSeenAt > :cutoff")
    List<Device> findRecentlyActiveDevices(Instant cutoff);

    @Query("SELECT d FROM Device d WHERE d.type.id = :typeId")
    List<Device> findByDeviceType(Long typeId);
}

