package com.smartsolutions.smart_solutions.controller;

import com.smartsolutions.smart_solutions.dto.DeviceDTO;
import com.smartsolutions.smart_solutions.dto.SensorDataDTO;
import com.smartsolutions.smart_solutions.model.*;
import com.smartsolutions.smart_solutions.repository.DeviceTypeRepository;
import com.smartsolutions.smart_solutions.repository.LocationRepository;
import com.smartsolutions.smart_solutions.repository.UserRepository;
import com.smartsolutions.smart_solutions.service.DeviceService;
import com.smartsolutions.smart_solutions.util.DeviceMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
@Tag(name = "Device Management", description = "Operations related to IoT device management")
@SecurityRequirement(name = "bearerAuth")
public class DeviceController {

    private final DeviceService deviceService;
    private final DeviceTypeRepository deviceTypeRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final DeviceMapper deviceMapper;

    @Operation(
            summary = "Get all devices",
            description = "Retrieves a paginated list of all registered devices",
            parameters = {
                    @Parameter(name = "page", description = "Page number (0-based)", example = "0"),
                    @Parameter(name = "size", description = "Page size", example = "20"),
                    @Parameter(name = "sort", description = "Sorting criteria (field,direction)", example = "name,asc")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved device list",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Page.class)
                    )
            )
    })
    @GetMapping("/getAllDevices")
    public ResponseEntity<Page<DeviceDTO>> getAllDevices(Pageable pageable) {
        return ResponseEntity.ok(deviceService.getAllDevices(pageable));
    }

    @Operation(
            summary = "Get device by ID",
            description = "Retrieves a single device by its unique identifier"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Device found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DeviceDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Device not found"
            )
    })
    @GetMapping("/getDeviceById/{id}")
    public ResponseEntity<DeviceDTO> getDeviceById(
            @Parameter(description = "ID of the device to retrieve", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(deviceService.getDeviceById(id));
    }

    @Operation(
            summary = "Create a new device",
            description = "Registers a new IoT device in the system"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Device created successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DeviceDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Referenced type/location/user not found"
            )
    })
    @PostMapping(
            value = "/createDevice",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<DeviceDTO> createDevice(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Device creation payload",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = DeviceDTO.class),
                            examples = @ExampleObject(
                                    value = "{\"name\": \"Thermostat-1\", \"typeId\": 1, \"locationId\": 1}"
                            )
                    )
            )
            @RequestBody @Valid DeviceDTO deviceDTO) {
        Device device = deviceMapper.toEntity(deviceDTO);

        // Set device type references
        if (deviceDTO.getLocationId() != null) {
            DeviceType deviceType = deviceTypeRepository.findById(deviceDTO.getTypeId())
                    .orElseThrow(() -> new EntityNotFoundException("Device type not found with id: " + deviceDTO.getTypeId()));
            device.setType(deviceType);
        }

        // Set user references
        if (deviceDTO.getCreatedBy() != null) {
            User createdBy = userRepository.findByName(deviceDTO.getCreatedBy())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + deviceDTO.getCreatedBy()));
            device.setCreatedBy(createdBy);
        }

        // Set location references
        if (deviceDTO.getLocationId() != null) {
            Location location = locationRepository.findById(deviceDTO.getLocationId())
                    .orElseThrow(() -> new EntityNotFoundException("Location not found with id: " + deviceDTO.getLocationId()));
            device.setLocation(location);
        }

        Device createdDevice = deviceService.createDevice(device);
        DeviceDTO createdDeviceDto = deviceMapper.toDto(createdDevice);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdDeviceDto.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdDeviceDto);
    }

    @Operation(
            summary = "Update device",
            description = "Updates an existing device's information"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Device updated successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DeviceDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Device not found"
            )
    })
    @PutMapping(
            value = "/updateDevice/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<DeviceDTO> updateDevice(
            @Parameter(description = "ID of the device to update", example = "1")
            @PathVariable Long id,
            @RequestBody @Valid DeviceDTO deviceDTO) {
        return ResponseEntity.ok(deviceService.updateDevice(id, deviceDTO));
    }

    @Operation(
            summary = "Delete device",
            description = "Removes a device from the system"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Device deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Device not found"
            )
    })
    @DeleteMapping("/deleteDevice/{id}")
    public ResponseEntity<Void> deleteDevice(
            @Parameter(description = "ID of the device to delete", example = "1")
            @PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get device sensor data",
            description = "Retrieves sensor readings for a specific device with optional filters",
            parameters = {
                    @Parameter(name = "deviceId", description = "Device ID", example = "1"),
                    @Parameter(name = "sensorType", description = "Filter by sensor type", example = "temperature"),
                    @Parameter(name = "startDate", description = "Filter readings after this date (ISO format)", example = "2023-01-01T00:00:00Z"),
                    @Parameter(name = "endDate", description = "Filter readings before this date (ISO format)", example = "2023-12-31T23:59:59Z"),
                    @Parameter(name = "page", description = "Page number (0-based)", example = "0"),
                    @Parameter(name = "size", description = "Page size", example = "20")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved sensor data",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Device not found"
            )
    })
    @GetMapping(
            value = "/sensor-data/{deviceId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Page<SensorDataDTO>> getDeviceSensorData(
            @PathVariable Long deviceId,
            @RequestParam(required = false) String sensorType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            Pageable pageable) {

        deviceService.validateDeviceExists(deviceId);
        Page<SensorDataDTO> data = deviceService.getDeviceSensorData(
                deviceId, sensorType, startDate, endDate, pageable);
        return ResponseEntity.ok(data);
    }

    @Operation(
            summary = "Get device sensor types",
            description = "Retrieves a list of all sensor types available for a device"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved sensor types",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = List.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Device not found"
            )
    })
    @GetMapping(
            value = "/sensors/{deviceId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<String>> getDeviceSensors(
            @Parameter(description = "ID of the device", example = "1")
            @PathVariable Long deviceId) {
        List<String> sensorTypes = deviceService.getDeviceSensorTypes(deviceId);
        return ResponseEntity.ok(sensorTypes);
    }

    @Operation(
            summary = "Add sensor reading",
            description = "Records a new sensor reading for a device"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Reading created successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SensorDataDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Device not found"
            )
    })
    @PostMapping(
            value = "/sensor-data/{deviceId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SensorDataDTO> addReading(
            @Parameter(description = "ID of the device", example = "1")
            @PathVariable Long deviceId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Sensor reading data",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SensorDataDTO.class),
                            examples = @ExampleObject(
                                    value = "{\"sensorType\": \"temperature\", \"value\": 23.5, \"unit\": \"Celsius\"}"
                            )
                    )
            )
            @RequestBody @Valid SensorDataDTO sensorDataDTO) {

        SensorDataDTO createdReading = deviceService.addReading(deviceId, sensorDataDTO);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdReading.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdReading);
    }
}
