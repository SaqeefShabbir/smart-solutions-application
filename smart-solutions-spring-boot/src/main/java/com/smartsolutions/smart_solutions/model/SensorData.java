package com.smartsolutions.smart_solutions.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "sensor_data", schema = "iot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "data_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sensor_data_device"))
    private Device device;

    @Column(name = "sensor_type", nullable = false, length = 50)
    private String sensorType;

    @Column(nullable = false, columnDefinition = "DOUBLE PRECISION")
    private Double value;

    @Column(nullable = false, length = 20)
    private String unit;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant timestamp;

    @Column(precision = 5, scale = 2)
    private BigDecimal accuracy;

    @Column(name = "status_code")
    private Integer statusCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    // Enum for common sensor types
    @Getter
    public enum SensorType {
        Temperature("Temperature"),
        Humidity("Humidity"),
        Pressure("Pressure"),
        Air_Quality("Air Quality"),
        Light_Intensity("Light Intensity"),
        Motion("Motion"),
        Voltage("Voltage"),
        Current("Current"),
        Power("Power"),
        OTHER("Other");

        private final String displayName;

        SensorType(String displayName) {
            this.displayName = displayName;
        }

    }

    // Business logic methods
    public boolean isValueWithinRange(Double min, Double max) {
        return value != null && value >= min && value <= max;
    }

    public String getFormattedValue() {
        return String.format("%.2f %s", value, unit);
    }

    public boolean hasWarningStatus() {
        return statusCode != null && statusCode >= 300;
    }
}