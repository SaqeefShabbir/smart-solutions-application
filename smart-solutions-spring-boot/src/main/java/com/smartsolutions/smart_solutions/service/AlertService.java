package com.smartsolutions.smart_solutions.service;

import com.smartsolutions.smart_solutions.dto.AlertDTO;
import com.smartsolutions.smart_solutions.dto.DeviceDTO;
import com.smartsolutions.smart_solutions.model.Alert;
import com.smartsolutions.smart_solutions.model.Device;
import com.smartsolutions.smart_solutions.model.User;
import com.smartsolutions.smart_solutions.repository.AlertRepository;
import com.smartsolutions.smart_solutions.repository.UserRepository;
import com.smartsolutions.smart_solutions.util.AlertMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final AlertMapper alertMapper;

    public Page<Alert> findAlertsWithFilters(
            Long deviceId,
            String alertType,
            Alert.Severity severity,
            Alert.AlertStatus status,
            Boolean isActive,
            Instant startDate,
            Instant endDate,
            Pageable pageable) {

        // Implementation would use Specifications or custom query
        return alertRepository.findWithFilters(
                deviceId, alertType, severity, status, isActive, startDate, endDate, pageable);
    }

    @Transactional
    public Alert createAlert(Alert alert) {
        return alertRepository.save(alert);
    }

    @Transactional
    public AlertDTO updateAlert(Long id, AlertDTO alertDTO) {
        Alert existingAlert = alertRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Alert with ID %d not found", id)));

        alertMapper.updateEntityFromDto(alertDTO, existingAlert);
        existingAlert.setUpdatedAt(Instant.now());
        return alertMapper.toDto(alertRepository.save(existingAlert));
    }


    @Transactional
    public Alert acknowledgeAlert(Long alertId, Long userId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        alert.setStatus(Alert.AlertStatus.Acknowledged);
        alert.setAcknowledgedBy(user);
        alert.setAcknowledgedAt(Instant.now());

        return alertRepository.save(alert);
    }

    @Transactional
    public Alert resolveAlert(Long alertId, Long userId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        alert.setStatus(Alert.AlertStatus.Resolved);
        alert.setResolvedBy(user);
        alert.setResolvedAt(Instant.now());

        return alertRepository.save(alert);
    }

    @Transactional
    public int acknowledgeMultipleAlerts(List<Long> alertIds, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return alertRepository.acknowledgeAlerts(
                alertIds, user, Instant.now());
    }

    public Map<Alert.Severity, Long> countAlertsBySeverity() {
        return alertRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        Alert::getSeverity,
                        Collectors.counting()
                ));
    }

    public Map<Alert.AlertStatus, Long> countAlertsByStatus() {
        return alertRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        Alert::getStatus,
                        Collectors.counting()
                ));
    }

    public List<Alert> findLatestDeviceAlerts(Long deviceId, int count) {
        return alertRepository.findLatestAlerts(deviceId, count)
                .stream()
                .limit(count)
                .collect(Collectors.toList());
    }

    public List<Alert> findCriticalUnacknowledgedAlerts() {
        return alertRepository.findCriticalUnacknowledgedAlerts(
                Alert.Severity.Critical, Alert.AlertStatus.Acknowledged);
    }
}