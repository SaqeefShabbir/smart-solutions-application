package com.smartsolutions.smart_solutions.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlertFilterDTO {
    private String severity;
    private String status; // "acknowledged", "unacknowledged", or empty for all
    private Long deviceId;
    private String dateRange; // "1h", "24h", "7d", "30d", "all"
}
