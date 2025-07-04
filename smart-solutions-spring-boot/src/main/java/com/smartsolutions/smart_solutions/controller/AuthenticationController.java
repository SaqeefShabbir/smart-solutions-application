package com.smartsolutions.smart_solutions.controller;

import com.smartsolutions.smart_solutions.dto.AuthenticationRequest;
import com.smartsolutions.smart_solutions.dto.AuthenticationResponse;
import com.smartsolutions.smart_solutions.dto.RegisterRequest;
import com.smartsolutions.smart_solutions.service.AuthenticationService;
import com.smartsolutions.smart_solutions.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration, authentication, and token verification")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account and returns authentication tokens"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User registered successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthenticationResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"token\": \"eyJhbGci...\", \"refreshToken\": \"eyJhbGci...\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid registration data"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Username or email already exists"
            )
    })
    @PostMapping(
            value = "/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AuthenticationResponse> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User registration details",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RegisterRequest.class),
                            examples = @ExampleObject(
                                    value = "{\"firstname\": \"John\", \"lastname\": \"Doe\", \"email\": \"john.doe@example.com\", \"password\": \"securePassword123\"}"
                            )
                    )
            )
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @Operation(
            summary = "Authenticate user",
            description = "Authenticates user credentials and returns JWT tokens"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthenticationResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"token\": \"eyJhbGci...\", \"refreshToken\": \"eyJhbGci...\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials"
            )
    })
    @PostMapping(
            value = "/authenticate",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AuthenticationResponse> authenticate(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User credentials",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = AuthenticationRequest.class),
                            examples = @ExampleObject(
                                    value = "{\"email\": \"john.doe@example.com\", \"password\": \"securePassword123\"}"
                            )
                    )
            )
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @Operation(
            summary = "Verify JWT token",
            description = "Validates a JWT token and returns user information if valid",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Token is valid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthenticationResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired token"
            )
    })
    @PostMapping(
            value = "/verify-token",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AuthenticationResponse> verifyToken(
            @Parameter(
                    name = "Authorization",
                    description = "Bearer token",
                    required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(authenticationService.verifyToken(authHeader));
    }
}
