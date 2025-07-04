package com.smartsolutions.smart_solutions.controller;

import com.smartsolutions.smart_solutions.dto.AlertDTO;
import com.smartsolutions.smart_solutions.model.Alert;
import com.smartsolutions.smart_solutions.model.Device;
import com.smartsolutions.smart_solutions.model.User;
import com.smartsolutions.smart_solutions.repository.AlertRepository;
import com.smartsolutions.smart_solutions.repository.DeviceRepository;
import com.smartsolutions.smart_solutions.repository.UserRepository;
import com.smartsolutions.smart_solutions.service.AlertService;
import com.smartsolutions.smart_solutions.util.AlertMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/alerts")
@Tag(name = "Alert Management", description = "Operations related to alert management")
@SecurityRequirement(name = "bearerAuth") // Applies to all operations
public class AlertController {

    private final AlertRepository alertRepository;
    private final AlertService alertService;
    private final AlertMapper alertMapper;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;

    public AlertController(AlertRepository alertRepository, UserRepository userRepository, DeviceRepository deviceRepository, AlertService alertService, AlertMapper alertMapper) {
        this.alertRepository = alertRepository;
        this.alertService = alertService;
        this.alertMapper = alertMapper;
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
    }

    @Operation(
            summary = "Get paginated alerts with filters",
            description = "Retrieve alerts with optional filtering by device, type, severity, status, etc.",
            parameters = {
                    @Parameter(name = "deviceId", description = "Filter by device ID", example = "1"),
                    @Parameter(name = "alertType", description = "Filter by alert type", example = "TEMPERATURE_HIGH"),
                    @Parameter(name = "severity", description = "Filter by severity level", schema = @Schema(implementation = Alert.Severity.class)),
                    @Parameter(name = "status", description = "Filter by alert status", schema = @Schema(implementation = Alert.AlertStatus.class)),
                    @Parameter(name = "isActive", description = "Filter active/inactive alerts", example = "true"),
                    @Parameter(name = "startDate", description = "Filter alerts after this date (ISO format)", example = "2024-01-01T00:00:00Z"),
                    @Parameter(name = "endDate", description = "Filter alerts before this date (ISO format)", example = "2024-12-31T23:59:59Z"),
                    @Parameter(name = "page", in = ParameterIn.QUERY, description = "Page number (0-based)", example = "0"),
                    @Parameter(name = "size", in = ParameterIn.QUERY, description = "Page size", example = "20"),
                    @Parameter(name = "sort", in = ParameterIn.QUERY, description = "Sorting criteria (field,direction)", example = "createdAt,desc")
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved alerts"),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/getAllAlerts")
    public ResponseEntity<Page<AlertDTO>> getAllAlerts(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) String alertType,
            @RequestParam(required = false) Alert.Severity severity,
            @RequestParam(required = false) Alert.AlertStatus status,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {

        Page<Alert> alerts = alertService.findAlertsWithFilters(
                deviceId,
                alertType,
                severity,
                status,
                isActive,
                startDate,
                endDate,
                pageable
        );
        Page<AlertDTO> dtoPage = alerts.map(alertMapper::toDto);
        return ResponseEntity.ok(dtoPage);
    }

    @Operation(
            summary = "Get alert by ID",
            description = "Retrieve a single alert by its unique identifier"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alert found"),
            @ApiResponse(responseCode = "404", description = "Alert not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AlertDTO> getAlertById(
            @Parameter(description = "ID of the alert to retrieve", example = "1")
            @PathVariable Long id) {
        return alertRepository.findById(id)
                .map(alertMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Create a new alert",
            description = "Creates a new alert with the provided details"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Alert created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Referenced device/user not found")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AlertDTO> createAlert(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Alert details to create",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = AlertDTO.class),
                    examples = @ExampleObject(
                            value = "{\"deviceId\": 1, \"alertType\": \"TEMPERATURE_HIGH\", \"severity\": \"CRITICAL\"}"
                    )
            )
    ) @RequestBody @Valid AlertDTO alertDTO) {
        Alert alert = alertMapper.toEntity(alertDTO);

        // Set device references
        if (alertDTO.getDeviceId() != null) {
            Device device = deviceRepository.findById(alertDTO.getDeviceId())
                    .orElseThrow(() -> new EntityNotFoundException("Device not found with id: " + alertDTO.getDeviceId()));
            alert.setDevice(device);
        }

        // Set user references
        if (alertDTO.getCreatedBy() != null) {
            User createdBy = userRepository.findByName(alertDTO.getCreatedBy())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + alertDTO.getCreatedBy()));
            alert.setCreatedBy(createdBy);
        }

        if (alertDTO.getAcknowledgedBy() != null) {
            User acknowledgedBy = userRepository.findByName(alertDTO.getAcknowledgedBy())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + alertDTO.getAcknowledgedBy()));
            alert.setAcknowledgedBy(acknowledgedBy);
        }

        if (alertDTO.getResolvedBy() != null) {
            User resolvedBy = userRepository.findByName(alertDTO.getResolvedBy())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + alertDTO.getResolvedBy()));
            alert.setResolvedBy(resolvedBy);
        }

        Alert createdAlert = alertService.createAlert(alert);
        AlertDTO createdAlertDto = alertMapper.toDto(createdAlert);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdAlertDto.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdAlertDto);
    }

    @Operation(
            summary = "Update an alert",
            description = "Updates an existing alert with new information"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alert updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Alert not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/updateAlert/{id}")
    public ResponseEntity<AlertDTO> updateDevice(
            @Parameter(description = "ID of the alert to update", example = "1")
            @PathVariable Long id,
            @RequestBody @Valid AlertDTO alertDTO) {
        return ResponseEntity.ok(alertService.updateAlert(id, alertDTO));
    }

    @Operation(
            summary = "Acknowledge an alert",
            description = "Marks an alert as acknowledged by the specified user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alert acknowledged successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID"),
            @ApiResponse(responseCode = "404", description = "Alert not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PatchMapping("/acknowledgeAlert/{id}")
    public ResponseEntity<AlertDTO> acknowledgeAlert(
            @Parameter(description = "ID of the alert to acknowledge", example = "1")
            @PathVariable Long id,
            @Parameter(description = "User ID from X-User-Id header", required = true)
            HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        Alert acknowledgedAlert = alertService.acknowledgeAlert(id, Long.valueOf(userIdStr));
        return ResponseEntity.ok(alertMapper.toDto(acknowledgedAlert));
    }

    @Operation(
            summary = "Resolve an alert",
            description = "Marks an alert as resolved by the specified user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alert resolved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID"),
            @ApiResponse(responseCode = "404", description = "Alert not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/resolveAlert/{id}")
    public ResponseEntity<AlertDTO> resolveAlert(
            @Parameter(description = "ID of the alert to resolve", example = "1")
            @PathVariable Long id,
            @Parameter(description = "User ID from X-User-Id header", required = true)
            HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        Alert resolvedAlert = alertService.resolveAlert(id, Long.valueOf(userIdStr));
        return ResponseEntity.ok(alertMapper.toDto(resolvedAlert));
    }

    @Operation(
            summary = "Batch acknowledge alerts",
            description = "Acknowledge multiple alerts at once"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alerts acknowledged successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/batch-acknowledge")
    public ResponseEntity<Integer> acknowledgeMultipleAlerts(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of alert IDs to acknowledge",
                    content = @Content(
                            examples = @ExampleObject(value = "[1, 2, 3]")
                    )
            )
            @RequestBody List<Long> alertIds,
            @Parameter(description = "User ID from X-User-Id header", required = true)
            HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        int updatedCount = alertService.acknowledgeMultipleAlerts(alertIds, Long.valueOf(userIdStr));
        return ResponseEntity.ok(updatedCount);
    }

    @Operation(
            summary = "Count alerts by severity",
            description = "Returns a count of alerts grouped by severity level"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved counts")
    @GetMapping("/count-by-severity")
    public ResponseEntity<Map<Alert.Severity, Long>> countAlertsBySeverity() {
        return ResponseEntity.ok(alertService.countAlertsBySeverity());
    }

    @Operation(
            summary = "Count alerts by status",
            description = "Returns a count of alerts grouped by status"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved counts")
    @GetMapping("/count-by-status")
    public ResponseEntity<Map<Alert.AlertStatus, Long>> countAlertsByStatus() {
        return ResponseEntity.ok(alertService.countAlertsByStatus());
    }

    @Operation(
            summary = "Get latest alerts for device",
            description = "Retrieves the most recent alerts for a specific device"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved alerts"),
            @ApiResponse(responseCode = "404", description = "Device not found")
    })
    @GetMapping("/device/latest/{deviceId}")
    public ResponseEntity<List<AlertDTO>> getLatestDeviceAlerts(
            @Parameter(description = "ID of the device", example = "1")
            @PathVariable Long deviceId,
            @Parameter(description = "Number of alerts to retrieve", example = "5")
            @RequestParam(defaultValue = "5") int count) {
        List<Alert> alerts = alertService.findLatestDeviceAlerts(deviceId, count);
        return ResponseEntity.ok(alertMapper.toDtoList(alerts));
    }

    @Operation(
            summary = "Get critical unacknowledged alerts",
            description = "Retrieves all critical alerts that haven't been acknowledged"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved alerts")
    @GetMapping("/critical-unacknowledged")
    public ResponseEntity<List<AlertDTO>> getCriticalUnacknowledgedAlerts() {
        List<Alert> alerts = alertService.findCriticalUnacknowledgedAlerts();
        return ResponseEntity.ok(alertMapper.toDtoList(alerts));
    }

    @Operation(
            summary = "Delete an alert",
            description = "Permanently deletes an alert from the system"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Alert deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Alert not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/deleteAlert/{id}")
    public ResponseEntity<Void> deleteAlert(
            @Parameter(description = "ID of the alert to delete", example = "1")
            @PathVariable Long id) {
        alertRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}