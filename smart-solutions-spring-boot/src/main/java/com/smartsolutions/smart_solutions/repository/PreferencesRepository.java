package com.smartsolutions.smart_solutions.repository;

import com.smartsolutions.smart_solutions.model.Preferences;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PreferencesRepository extends JpaRepository<Preferences, Long> {
    Optional<Preferences> findByUserId(Long userId);
}
