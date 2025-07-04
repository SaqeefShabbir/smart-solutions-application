package com.smartsolutions.smart_solutions.controller;

import com.smartsolutions.smart_solutions.dto.UserDTO;
import com.smartsolutions.smart_solutions.exception.ResourceNotFoundException;
import com.smartsolutions.smart_solutions.model.NotificationSettings;
import com.smartsolutions.smart_solutions.model.Preferences;
import com.smartsolutions.smart_solutions.model.User;
import com.smartsolutions.smart_solutions.repository.NotificationSettingsRepository;
import com.smartsolutions.smart_solutions.repository.PreferencesRepository;
import com.smartsolutions.smart_solutions.repository.UserRepository;
import com.smartsolutions.smart_solutions.util.UserMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "Operations related to user data")
public class UserController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final NotificationSettingsRepository notificationSettingsRepository;
    private final PreferencesRepository preferencesRepository;

    @Operation(
            summary = "Get all users",
            description = "Retrieves a list of all registered users in the system",
            parameters = {}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved user list",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = User[].class),
                            examples = @ExampleObject(
                                    value = "[{\"id\": 1, \"username\": \"john_doe\", \"email\": \"john@example.com\"}]"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "No users found",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    @GetMapping(
            value = "/getAllUsers",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<UserDTO> userDTOS = users.stream().map(userMapper::toDto).toList();
        return ResponseEntity.ok(userDTOS);
    }

    @Operation(
            summary = "Get user by ID",
            description = "Retrieves a single user by their unique identifier",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "ID of the user to retrieve",
                            required = true,
                            example = "1",
                            schema = @Schema(type = "integer", minimum = "1")
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = User.class),
                            examples = @ExampleObject(
                                    value = "{\"id\": 1, \"username\": \"john_doe\", \"email\": \"john@example.com\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid ID supplied"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    @GetMapping(
            value = "/getUserById/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserDTO> getUserById(
            @Parameter(description = "ID of user to be retrieved")
            @PathVariable Long id) {
        if (id == null || id < 1) {
            return ResponseEntity.badRequest().build();
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        UserDTO userDTO = userMapper.toDto(user);

        NotificationSettings notificationSettings = notificationSettingsRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultNotificationSettings(user));

        notificationSettings.setUser(null);

        Preferences preferences = preferencesRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultPreferences(user));

        preferences.setUser(null);

        userDTO.setNotificationSettings(notificationSettings);
        userDTO.setPreferences(preferences);
        return ResponseEntity.ok(userDTO);
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
