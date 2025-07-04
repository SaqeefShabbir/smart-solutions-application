package com.smartsolutions.smart_solutions.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.smartsolutions.smart_solutions.model.SensorData.SensorType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Data
public class SensorDataDTO {
    private Long id;
    private Long deviceId;
    private String deviceName;
    private SensorType sensorType;
    private Double value;
    private String unit;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant timestamp;

    private BigDecimal accuracy;
    private Integer statusCode;
    private Map<String, Object> metadata;
    private String formattedValue;
    private Boolean hasWarning;
}