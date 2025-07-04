package com.smartsolutions.smart_solutions.util;

import com.smartsolutions.smart_solutions.dto.DeviceDTO;
import com.smartsolutions.smart_solutions.model.Device;
import com.smartsolutions.smart_solutions.model.DeviceType;
import com.smartsolutions.smart_solutions.model.User;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class DeviceMapper {

    public DeviceDTO toDto(Device device) {
        if (device == null) {
            return null;
        }

        DeviceDTO dto = new DeviceDTO();
        dto.setId(device.getId());
        dto.setName(device.getName());

        // Handle DeviceType mapping
        if (device.getType() != null) {
            dto.setType(device.getType().getTypeName());
            dto.setTypeId(device.getType().getId());
        }

        dto.setSerialNumber(device.getSerialNumber());
        dto.setMacAddress(device.getMacAddress());
        dto.setIpAddress(device.getIpAddress());
        dto.setStatus(device.getStatus().name());
        dto.setOnline(device.getIsOnline());

        dto.setAlertsCount(device.getAlerts().size());

        // Convert Instant to LocalDateTime
        if (device.getLastSeenAt() != null) {
            dto.setLastSeenAt(convertToLocalDateTime(device.getLastSeenAt()));
        }

        dto.setFirmwareVersion(device.getFirmwareVersion());
        // Location references
        if (device.getLocation() != null) {
            dto.setLocationId(device.getLocation().getId());
            dto.setLocation(device.getLocation().getName());
            dto.setLatitude(device.getLocation().getLatitude());
            dto.setLongitude(device.getLocation().getLongitude());
        }

        // Handle CreatedBy mapping
        if (device.getCreatedBy() != null) {
            dto.setCreatedBy(device.getCreatedBy().getName());
            dto.setCreatedById(device.getCreatedBy().getId());
        }

        // Timestamp conversions
        dto.setCreatedAt(convertToLocalDateTime(device.getCreatedAt()));
        dto.setUpdatedAt(convertToLocalDateTime(device.getUpdatedAt()));

        // Direct map for metadata
        dto.setMetadata(device.getMetadata());

        return dto;
    }

    public Device toEntity(DeviceDTO dto) {
        if (dto == null) {
            return null;
        }

        Device device = new Device();
        device.setId(dto.getId());
        device.setName(dto.getName());

        // Handle DeviceType - assumes you'll set the full entity elsewhere
        if (dto.getTypeId() != null) {
            DeviceType type = new DeviceType();
            type.setId(dto.getTypeId());
            device.setType(type);
        }

        device.setSerialNumber(dto.getSerialNumber());
        device.setMacAddress(dto.getMacAddress());
        device.setIpAddress(dto.getIpAddress());

        // Handle status enum
        if (dto.getStatus() != null) {
            device.setStatus(Device.DeviceStatus.valueOf(dto.getStatus()));
        }

        device.setIsOnline(dto.isOnline());

        // Convert LocalDateTime to Instant
        if (dto.getLastSeenAt() != null) {
            device.setLastSeenAt(convertToInstant(dto.getLastSeenAt()));
        }

        device.setFirmwareVersion(dto.getFirmwareVersion());

        // Handle CreatedBy - assumes you'll set the full entity elsewhere
//        if (dto.getCreatedById() != null) {
//            User user = new User();
//            user.setId(dto.getCreatedById());
//            device.setCreatedBy(user);
//        }

        // Metadata direct map
        device.setMetadata(dto.getMetadata());

        return device;
    }

    public void updateEntityFromDto(DeviceDTO dto, Device entity) {
        if (dto == null || entity == null) {
            return;
        }

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }

        if (dto.getSerialNumber() != null) {
            entity.setSerialNumber(dto.getSerialNumber());
        }

        if (dto.getMacAddress() != null) {
            entity.setMacAddress(dto.getMacAddress());
        }

        if (dto.getIpAddress() != null) {
            entity.setIpAddress(dto.getIpAddress());
        }

        if (dto.getStatus() != null) {
            entity.setStatus(Device.DeviceStatus.valueOf(dto.getStatus()));
        }

        if (dto.getLastSeenAt() != null) {
            entity.setLastSeenAt(convertToInstant(dto.getLastSeenAt()));
        }

        if (dto.getFirmwareVersion() != null) {
            entity.setFirmwareVersion(dto.getFirmwareVersion());
        }

        if (dto.getMetadata() != null) {
            entity.setMetadata(dto.getMetadata());
        }
    }

    private LocalDateTime convertToLocalDateTime(Instant instant) {
        return instant != null ? instant.atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
    }

    private Instant convertToInstant(LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
    }
}