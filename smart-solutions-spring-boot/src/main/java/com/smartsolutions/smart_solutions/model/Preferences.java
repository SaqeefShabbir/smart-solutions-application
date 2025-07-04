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
@Table(name = "user_preferences", schema = "iot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Preferences {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preference_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'light'")
    private String theme = "light";

    @Column(nullable = false, columnDefinition = "VARCHAR(10) DEFAULT 'en'")
    private String language = "en";

    @Column(nullable = false, columnDefinition = "VARCHAR(50) DEFAULT 'UTC'")
    private String timezone = "UTC";

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private Instant updatedAt;
}