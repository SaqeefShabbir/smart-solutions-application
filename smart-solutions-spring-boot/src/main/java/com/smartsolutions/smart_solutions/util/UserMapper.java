package com.smartsolutions.smart_solutions.util;

import com.smartsolutions.smart_solutions.dto.UserDTO;
import com.smartsolutions.smart_solutions.model.Role;
import com.smartsolutions.smart_solutions.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public UserMapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    // Convert User Entity to UserDTO
    public UserDTO toDto(User user) {
        if (user == null) {
            return null;
        }

        return UserDTO.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .username(user.getName())
                .email(user.getEmail())
                .password(user.getPassword())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .roles(mapRolesToStrings(user.getRoles()))
                .build();
    }

    // Convert UserDTO to User Entity (without roles and password)
    public User toEntity(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }

        User user = new User();
        user.setId(userDTO.getId());
        user.setFirstname(userDTO.getFirstname());
        user.setLastname(userDTO.getLastname());
        user.setName(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setActive(userDTO.isActive());
        user.setCreatedAt(userDTO.getCreatedAt());
        user.setUpdatedAt(userDTO.getUpdatedAt());
        user.setLastLoginAt(userDTO.getLastLoginAt());

        // Note: Roles and password should be handled separately
        return user;
    }

    // For creating new user with password encoding
    public User toNewEntity(UserDTO userDTO) {
        User user = toEntity(userDTO);
        if (userDTO.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        return user;
    }

    // Update existing user entity from DTO
    public void updateEntity(UserDTO userDTO, User user) {
        if (userDTO == null || user == null) {
            return;
        }

        user.setFirstname(userDTO.getFirstname());
        user.setLastname(userDTO.getLastname());
        user.setName(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setActive(userDTO.isActive());
        user.setUpdatedAt(Instant.now());
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

    // Helper method to convert role names to Role entities
    public Set<Role> mapStringsToRoles(Set<String> roleNames) {
        if (roleNames == null) {
            return Set.of();
        }
        return roleNames.stream()
                .map(roleName -> {
                    Role role = new Role();
                    role.setName(roleName);
                    return role;
                })
                .collect(Collectors.toSet());
    }
}