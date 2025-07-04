package com.smartsolutions.smart_solutions.controller;

import com.smartsolutions.smart_solutions.dto.*;
import com.smartsolutions.smart_solutions.exception.BadRequestException;
import com.smartsolutions.smart_solutions.exception.ResourceNotFoundException;
import com.smartsolutions.smart_solutions.model.NotificationSettings;
import com.smartsolutions.smart_solutions.model.Preferences;
import com.smartsolutions.smart_solutions.model.User;
import com.smartsolutions.smart_solutions.repository.NotificationSettingsRepository;
import com.smartsolutions.smart_solutions.repository.PreferencesRepository;
import com.smartsolutions.smart_solutions.repository.UserRepository;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Operations related to user profile management")
@SecurityRequirement(name = "bearerAuth") // Applies to all operations
public class UserProfileController {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final NotificationSettingsRepository notificationSettingsRepository;
    private final PreferencesRepository preferencesRepository;

    @Operation(
            summary = "Update user profile",
            description = "Updates the authenticated user's profile information",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile updated successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"success\": true, \"message\": \"Profile updated successfully\", \"data\": {\"id\": 1, \"firstName\": \"John\", \"lastName\": \"Doe\", \"email\": \"john.doe@example.com\"}}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    @PatchMapping(
            value = "/profile",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponseDTO> updateProfile(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Profile update request",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UpdateProfileRequest.class),
                            examples = @ExampleObject(
                                    value = "{\"firstName\": \"John\", \"lastName\": \"Doe\", \"email\": \"john.doe@example.com\"}"
                            )
                    )
            )
            @Valid @RequestBody UpdateProfileRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setFirstname(request.getFirstName());
        user.setLastname(request.getLastName());
        user.setName(request.getFirstName());
        user.setEmail(request.getEmail());

        User updatedUser = userRepository.save(user);

        NotificationSettings notificationSettings = notificationSettingsRepository.findByUserId(updatedUser.getId())
                .orElseGet(() -> createDefaultNotificationSettings(user));

        Preferences preferences = preferencesRepository.findByUserId(updatedUser.getId())
                .orElseGet(() -> createDefaultPreferences(user));

        if(Objects.nonNull(request.getNotificationSettings()))
            notificationSettings = updateNotificationSettings(updatedUser.getId(), request.getNotificationSettings());

        if(Objects.nonNull(request.getPreferences()))
            preferences = updatePreferences(updatedUser.getId(), request.getPreferences());

        notificationSettings.setUser(null);
        preferences.setUser(null);

        return ResponseEntity.ok(new ApiResponseDTO(
                true,
                "Profile updated successfully",
                new UserProfileResponse(updatedUser, notificationSettings, preferences))
        );
    }

    @Operation(
            summary = "Change password",
            description = "Changes the authenticated user's password",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Password changed successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"success\": true, \"message\": \"Password changed successfully\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid current password or new password matches current"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    @PatchMapping(
            value = "/password",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponseDTO> changePassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Password change request",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ChangePasswordRequest.class),
                            examples = @ExampleObject(
                                    value = "{\"currentPassword\": \"oldPassword123\", \"newPassword\": \"newSecurePassword456\"}"
                            )
                    )
            )
            @Valid @RequestBody ChangePasswordRequest request,
            @Parameter(
                    name = "X-User-Id",
                    description = "User ID from authentication token",
                    required = true,
                    example = "123",
                    schema = @Schema(type = "string")
            )
            HttpServletRequest httpServletRequest) {

        String userIdStr = httpServletRequest.getHeader("X-User-Id");

        User user = userRepository.findById(Long.valueOf(userIdStr))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        if (request.getNewPassword().equals(request.getCurrentPassword())) {
            throw new BadRequestException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(new ApiResponseDTO(true, "Password changed successfully"));
    }

    public NotificationSettings updateNotificationSettings(Long userId, NotificationSettings settingsDTO) {
        NotificationSettings settings = notificationSettingsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                    return new NotificationSettings();
                });

        settings.setEmailAlerts(settingsDTO.isEmailAlerts());
        settings.setPushNotifications(settingsDTO.isPushNotifications());
        settings.setSmsAlerts(settingsDTO.isSmsAlerts());
        settings.setCriticalOnly(settingsDTO.isCriticalOnly());

        return notificationSettingsRepository.save(settings);
    }

    public Preferences updatePreferences(Long userId, Preferences preferencesDTO) {
        Preferences preferences = preferencesRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                    return new Preferences();
                });

        preferences.setTheme(preferencesDTO.getTheme());
        preferences.setLanguage(preferencesDTO.getLanguage());
        preferences.setTimezone(preferencesDTO.getTimezone());

        return preferencesRepository.save(preferences);
    }

    private NotificationSettings createDefaultNotificationSettings(User user) {
        NotificationSettings settings = new NotificationSettings();
        settings.setUser(user);
        settings.setEmailAlerts(true);
        settings.setPushNotifications(true);
        settings.setSmsAlerts(false);
        settings.setCriticalOnly(false);
        return notificationSettingsRepository.save(settings);
    }

    private Preferences createDefaultPreferences(User user) {
        Preferences preferences = new Preferences();
        preferences.setUser(user);
        preferences.setTheme("light");
        preferences.setLanguage("en");
        preferences.setTimezone("UTC");
        return preferencesRepository.save(preferences);
    }
}
