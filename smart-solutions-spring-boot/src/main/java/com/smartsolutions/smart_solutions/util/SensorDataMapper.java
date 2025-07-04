package com.smartsolutions.smart_solutions.util;

import com.smartsolutions.smart_solutions.dto.SensorDataDTO;
import com.smartsolutions.smart_solutions.model.Device;
import com.smartsolutions.smart_solutions.model.SensorData;
import com.smartsolutions.smart_solutions.model.SensorData.SensorType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SensorDataMapper {

    public SensorDataDTO toDto(SensorData entity) {
        if (entity == null) {
            return null;
        }

        SensorDataDTO dto = new SensorDataDTO();
        dto.setId(entity.getId());

        // Device mapping
        if (entity.getDevice() != null) {
            dto.setDeviceId(entity.getDevice().getId());
            dto.setDeviceName(entity.getDevice().getName());
        }

        // Sensor type mapping
        try {
            dto.setSensorType(SensorType.valueOf(entity.getSensorType()));
        } catch (IllegalArgumentException e) {
            dto.setSensorType(SensorType.OTHER);
        }

        dto.setValue(entity.getValue());
        dto.setUnit(entity.getUnit());
        dto.setTimestamp(entity.getTimestamp());
        dto.setAccuracy(entity.getAccuracy());
        dto.setStatusCode(entity.getStatusCode());
        dto.setMetadata(entity.getMetadata());

        // Calculated fields
        dto.setFormattedValue(entity.getFormattedValue());
        dto.setHasWarning(entity.hasWarningStatus());

        return dto;
    }

    public SensorData toEntity(SensorDataDTO dto) {
        if (dto == null) {
            return null;
        }

        SensorData entity = new SensorData();
        entity.setId(dto.getId());

        // Device mapping
        if (dto.getDeviceId() != null) {
            Device device = new Device();
            device.setId(dto.getDeviceId());
            entity.setDevice(device);
        }

        entity.setSensorType(dto.getSensorType() != null ?
                dto.getSensorType().name() :
                SensorType.OTHER.name());

        entity.setValue(dto.getValue());
        entity.setUnit(dto.getUnit());
        entity.setAccuracy(dto.getAccuracy());
        entity.setStatusCode(dto.getStatusCode() != null ? dto.getStatusCode() : 200); // Default status
        entity.setMetadata(dto.getMetadata());

        // Timestamp is automatically set by @CreationTimestamp

        return entity;
    }

    public List<SensorDataDTO> toDtoList(List<SensorData> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<SensorData> toEntityList(List<SensorDataDTO> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public void updateEntityFromDto(SensorData entity, SensorDataDTO dto) {
        if (entity == null || dto == null) {
            return;
        }

        // Don't update ID or timestamp
        if (dto.getDeviceId() != null) {
            if (entity.getDevice() == null) {
                entity.setDevice(new Device());
            }
            entity.getDevice().setId(dto.getDeviceId());
        }

        if (dto.getSensorType() != null) {
            entity.setSensorType(dto.getSensorType().name());
        }

        if (dto.getValue() != null) {
            entity.setValue(dto.getValue());
        }

        if (dto.getUnit() != null) {
            entity.setUnit(dto.getUnit());
        }

        if (dto.getAccuracy() != null) {
            entity.setAccuracy(dto.getAccuracy());
        }

        if (dto.getStatusCode() != null) {
            entity.setStatusCode(dto.getStatusCode());
        }

        if (dto.getMetadata() != null) {
            entity.setMetadata(dto.getMetadata());
        }
    }
}