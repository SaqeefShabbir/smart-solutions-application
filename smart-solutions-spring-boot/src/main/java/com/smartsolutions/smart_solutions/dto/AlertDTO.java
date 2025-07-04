package com.smartsolutions.smart_solutions.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
public class AlertDTO {
    private Long id;
    private String alertType;
    private String severity;
    private String message;
    private String status;
    private boolean acknowledged;
    private boolean isActive;
    private String createdBy;
    private String acknowledgedBy;
    private String resolvedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime resolvedAt;
    private Long deviceId;
    private String deviceName;
    private String deviceType;
    private Map<String, Object> additionalData;
}
