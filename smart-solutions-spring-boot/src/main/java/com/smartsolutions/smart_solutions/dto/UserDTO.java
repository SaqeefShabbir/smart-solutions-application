package com.smartsolutions.smart_solutions.dto;

import com.smartsolutions.smart_solutions.model.NotificationSettings;
import com.smartsolutions.smart_solutions.model.Preferences;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String firstname;
    private String lastname;
    private String username;
    private String email;
    private String password;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;
    private Set<String> roles;
    private NotificationSettings notificationSettings;
    private Preferences preferences;
}