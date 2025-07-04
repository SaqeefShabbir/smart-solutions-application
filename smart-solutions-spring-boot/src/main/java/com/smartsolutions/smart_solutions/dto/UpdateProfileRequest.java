package com.smartsolutions.smart_solutions.dto;

import com.smartsolutions.smart_solutions.model.NotificationSettings;
import com.smartsolutions.smart_solutions.model.Preferences;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String firstName;

    private String lastName;

    private String email;

    private NotificationSettings notificationSettings;

    private Preferences preferences;
}
