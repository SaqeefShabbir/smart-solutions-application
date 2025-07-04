package com.smartsolutions.smart_solutions.dto;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
public class DeviceDTO {
    private Long id;
    private String name;
    private Long typeId;
    private String type;
    private String serialNumber;
    private String macAddress;
    private String ipAddress;
    private String status;
    private boolean isOnline;
    private LocalDateTime lastSeenAt;
    private String firmwareVersion;
    private Long locationId;
    private String location;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer alertsCount;
    private String createdBy;
    private Long createdById;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Object> metadata;
}