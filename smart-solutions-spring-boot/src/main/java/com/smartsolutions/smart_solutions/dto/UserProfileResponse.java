package com.smartsolutions.smart_solutions.dto;

import com.smartsolutions.smart_solutions.model.NotificationSettings;
import com.smartsolutions.smart_solutions.model.Preferences;
import com.smartsolutions.smart_solutions.model.Role;
import com.smartsolutions.smart_solutions.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String firstname;
    private String lastname;
    private String name;
    private String email;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;
    private Set<String> roles;
    private NotificationSettings notificationSettings;
    private Preferences preferences;

    public UserProfileResponse(User user, NotificationSettings notificationSettings, Preferences preferences) {
        this.id = user.getId();
        this.firstname = user.getFirstname();
        this.lastname = user.getLastname();
        this.name = user.getName();
        this.email = user.getEmail();
        this.active = user.isActive();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.lastLoginAt = user.getLastLoginAt();
        this.roles = mapRolesToStrings(user.getRoles());
        this.notificationSettings = notificationSettings;
        this.preferences = preferences;
    }

    // Helper method to convert Role entities to role names
    private Set<String> mapRolesToStrings(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
