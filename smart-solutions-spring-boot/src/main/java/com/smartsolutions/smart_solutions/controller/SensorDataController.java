package com.smartsolutions.smart_solutions.controller;

import com.smartsolutions.smart_solutions.dto.SensorDataDTO;
import com.smartsolutions.smart_solutions.service.SensorDataService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sensor-data")
@RequiredArgsConstructor
@Tag(name = "Sensor Data", description = "Operations related to sensor data readings")
public class SensorDataController {
    private final SensorDataService sensorDataService;

    @Operation(
            summary = "Get latest sensor readings",
            description = "Retrieves the most recent sensor readings with optional count parameter",
            parameters = {
                    @Parameter(
                            name = "count",
                            description = "Number of latest readings to retrieve (default: 10)",
                            example = "5",
                            schema = @Schema(type = "integer", minimum = "1", maximum = "100")
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved sensor readings",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SensorDataDTO[].class),
                            examples = @ExampleObject(
                                    value = "[{\"sensorId\": 1, \"value\": 25.4, \"timestamp\": \"2023-05-15T10:30:00Z\"}]"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid count parameter (must be between 1-100)"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    @GetMapping(
            value = "/latest",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<SensorDataDTO>> getLatestReadings(
            @RequestParam(defaultValue = "10") int count) {

        List<SensorDataDTO> latestReadings = sensorDataService.getLatestReadings(count);
        return ResponseEntity.ok(latestReadings);
    }
}