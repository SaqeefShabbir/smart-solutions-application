package com.smartsolutions.smart_solutions.util;

import com.smartsolutions.smart_solutions.dto.AlertDTO;
import com.smartsolutions.smart_solutions.model.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class AlertMapper {

    private final ZoneId zoneId = ZoneId.systemDefault();

    public AlertDTO toDto(Alert alert) {
        if (alert == null) {
            return null;
        }

        AlertDTO dto = new AlertDTO();

        // Basic fields
        dto.setId(alert.getId());
        dto.setAlertType(alert.getAlertType());
        dto.setMessage(alert.getMessage());
        dto.setActive(alert.isActive());

        // Enum conversions
        if (alert.getSeverity() != null) {
            dto.setSeverity(alert.getSeverity().name());
        }
        if (alert.getStatus() != null) {
            dto.setStatus(alert.getStatus().name());
        }

        // Device references
        if (alert.getDevice() != null) {
            dto.setDeviceId(alert.getDevice().getId());
            dto.setDeviceName(alert.getDevice().getName());
            if (alert.getDevice().getType() != null) {
                dto.setDeviceType(alert.getDevice().getType().getTypeName());
            }
        }

        // User references
        if (alert.getCreatedBy() != null) {
            dto.setCreatedBy(alert.getCreatedBy().getName());
        }
        if (alert.getAcknowledgedBy() != null) {
            dto.setAcknowledged(Boolean.TRUE);
            dto.setAcknowledgedBy(alert.getAcknowledgedBy().getName());
        }
        if (alert.getResolvedBy() != null) {
            dto.setResolvedBy(alert.getResolvedBy().getName());
        }

        // Timestamp conversions with null checks
        dto.setCreatedAt(convertToLocalDateTime(alert.getCreatedAt()));
        dto.setUpdatedAt(convertToLocalDateTime(alert.getUpdatedAt()));
        dto.setAcknowledgedAt(convertToLocalDateTime(alert.getAcknowledgedAt()));
        dto.setResolvedAt(convertToLocalDateTime(alert.getResolvedAt()));

        // Additional data
        dto.setAdditionalData(alert.getAdditionalData());

        return dto;
    }

    public Alert toEntity(AlertDTO dto) {
        if (dto == null) {
            return null;
        }

        Alert alert = new Alert();

        // Basic fields
        alert.setId(dto.getId());
        alert.setAlertType(dto.getAlertType());
        alert.setMessage(dto.getMessage());

        // Enum conversions
        if (dto.getSeverity() != null) {
            alert.setSeverity(Alert.Severity.valueOf(dto.getSeverity()));
        }
        if (dto.getStatus() != null) {
            alert.setStatus(Alert.AlertStatus.valueOf(dto.getStatus()));
        }

        // Note: For toEntity conversions, you typically don't set relationships here
        // as they should be handled by the service layer with proper repository lookups

        // Set timestamps (typically managed by entity lifecycle)
        alert.setCreatedAt(dto.getCreatedAt() != null ?
                dto.getCreatedAt().atZone(zoneId).toInstant() : null);
        alert.setUpdatedAt(dto.getUpdatedAt() != null ?
                dto.getUpdatedAt().atZone(zoneId).toInstant() : null);
        alert.setAcknowledgedAt(dto.getAcknowledgedAt() != null ?
                dto.getAcknowledgedAt().atZone(zoneId).toInstant() : null);
        alert.setResolvedAt(dto.getResolvedAt() != null ?
                dto.getResolvedAt().atZone(zoneId).toInstant() : null);

        // Additional data
        alert.setAdditionalData(dto.getAdditionalData());

        return alert;
    }

    private LocalDateTime convertToLocalDateTime(Instant instant) {
        return instant != null ? instant.atZone(zoneId).toLocalDateTime() : null;
    }

    public List<AlertDTO> toDtoList(List<Alert> alerts) {
        return alerts.stream()
                .map(this::toDto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Page<AlertDTO> toDtoPage(Page<Alert> alertPage) {
        return alertPage.map(this::toDto);
    }

    public void updateEntityFromDto(AlertDTO dto, Alert entity) {
        if (dto == null || entity == null) {
            return;
        }

        // Only update what should be updatable
        if (dto.getMessage() != null) {
            entity.setMessage(dto.getMessage());
        }
        if (dto.getAdditionalData() != null) {
            entity.setAdditionalData(dto.getAdditionalData());
        }
        // Add other fields that should be updatable
    }
}