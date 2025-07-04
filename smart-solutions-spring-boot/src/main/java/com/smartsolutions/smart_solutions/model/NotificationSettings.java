package com.smartsolutions.smart_solutions.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;


@Entity
@Table(name = "user_notification_settings", schema = "iot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "email_alerts", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean emailAlerts = true;

    @Column(name = "push_notifications", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean pushNotifications = true;

    @Column(name = "sms_alerts", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean smsAlerts = false;

    @Column(name = "critical_only", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean criticalOnly = false;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private Instant updatedAt;
}
