package com.smartsolutions.smart_solutions.service;

import com.smartsolutions.smart_solutions.dto.AuthenticationRequest;
import com.smartsolutions.smart_solutions.dto.AuthenticationResponse;
import com.smartsolutions.smart_solutions.dto.RegisterRequest;
import com.smartsolutions.smart_solutions.model.NotificationSettings;
import com.smartsolutions.smart_solutions.model.Preferences;
import com.smartsolutions.smart_solutions.model.Role;
import com.smartsolutions.smart_solutions.model.User;
import com.smartsolutions.smart_solutions.repository.NotificationSettingsRepository;
import com.smartsolutions.smart_solutions.repository.PreferencesRepository;
import com.smartsolutions.smart_solutions.repository.RoleRepository;
import com.smartsolutions.smart_solutions.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.management.relation.RoleNotFoundException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final NotificationSettingsRepository notificationSettingsRepository;
    private final PreferencesRepository preferencesRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        var user = User.builder()
                .firstname(request.getFirstName())
                .lastname(request.getLastName())
                .name(request.getFirstName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .active(Boolean.TRUE)
                .build();

        Set<String> roleNames = new HashSet<>();

        roleNames.add("OPERATOR");

        Set<Role> roles = roleNames.stream()
                .map(roleName -> {
                    try {
                        return roleRepository.findByName(roleName)
                                .orElseThrow(() -> new RoleNotFoundException(roleName));
                    } catch (RoleNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toSet());

        user.setRoles(roles);
        userRepository.save(user);

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .userId(user.getId())
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found with email" + request.getEmail()));

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .userId(user.getId())
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse verifyToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7);

        try {
            String userEmail = jwtService.extractUsername(token);
            var user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with email" + userEmail));

            if (userEmail != null && jwtService.isTokenValid(token, user)) {
                return AuthenticationResponse.builder()
                        .token(token)
                        .build();
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
