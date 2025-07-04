package com.smartsolutions.smart_solutions.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "devices", schema = "iot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_id")
    private Long id;

    @Column(name = "device_name", nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", referencedColumnName = "type_id")
    private DeviceType type;

    @Column(name = "serial_number", unique = true, length = 100)
    private String serialNumber;

    @Column(name = "mac_address", unique = true, length = 17)
    private String macAddress;

    @Column(name = "ip_address", length = 15)
    private String ipAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", referencedColumnName = "location_id", foreignKey = @ForeignKey(name = "fk_device_location"))
    private Location location;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DeviceStatus status = DeviceStatus.Inactive;

    @Column(name = "is_online")
    private Boolean isOnline = false;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @Column(name = "firmware_version", length = 50)
    private String firmwareVersion;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "user_id")
    private User createdBy;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Alert> alerts = new ArrayList<>();

    @Getter
    public enum DeviceStatus {
        Active("Active"), Inactive("Inactive"), Maintenance("Maintenance"), Retired("Retired");

        private final String displayName;

        DeviceStatus(String displayName)
        {
            this.displayName = displayName;
        }
    }

    // Business logic methods
    public void markOnline() {
        this.isOnline = true;
        this.lastSeenAt = Instant.now();
        if (this.status == DeviceStatus.Inactive) {
            this.status = DeviceStatus.Active;
        }
    }

    public void markOffline() {
        this.isOnline = false;
        this.lastSeenAt = Instant.now();
    }

    public void putMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }

    public Object getMetadata(String key) {
        return this.metadata != null ? this.metadata.get(key) : null;
    }
}