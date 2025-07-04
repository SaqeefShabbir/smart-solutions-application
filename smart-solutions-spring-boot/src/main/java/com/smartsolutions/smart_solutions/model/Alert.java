package com.smartsolutions.smart_solutions.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "device_alerts", schema = "iot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "alert_type", nullable = false, length = 50)
    private String alertType;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Column(nullable = false)
    private String message;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> additionalData;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertStatus status = AlertStatus.Open;

    @Column(name = "is_active", insertable = false, updatable = false)
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "user_id")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acknowledged_by", referencedColumnName = "user_id")
    private User acknowledgedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by", referencedColumnName = "user_id")
    private User resolvedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Getter
    public enum Severity {
        Low("Low"), Medium("Medium"), High("High"), Critical("Critical");

        private final String displayName;

        Severity(String displayName) {
            this.displayName = displayName;
        }
    }

    @Getter
    public enum AlertStatus {
        Open("Open"), Acknowledged("Acknowledged"), Resolved("Resolved"), Suppressed("Suppressed"), Unacknowledged("Unacknowledged");

        private final String displayName;

        AlertStatus(String displayName) {
            this.displayName = displayName;
        }
    }

    // Business logic methods
    public void acknowledge(User user) {
        this.status = AlertStatus.Acknowledged;
        this.acknowledgedBy = user;
        this.acknowledgedAt = Instant.now();
    }

    public void resolve(User user) {
        this.status = AlertStatus.Resolved;
        this.resolvedBy = user;
        this.resolvedAt = Instant.now();
    }

    public boolean isActive() {
        return status == AlertStatus.Open || status == AlertStatus.Acknowledged;
    }
}
