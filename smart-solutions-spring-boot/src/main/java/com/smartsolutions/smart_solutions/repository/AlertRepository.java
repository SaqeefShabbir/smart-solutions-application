package com.smartsolutions.smart_solutions.repository;

import com.smartsolutions.smart_solutions.model.Alert;
import com.smartsolutions.smart_solutions.model.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long>, JpaSpecificationExecutor<Alert> {

    // Basic CRUD operations are provided by JpaRepository

    // Find by device
    List<Alert> findByDeviceId(Long deviceId);

    // Find by status
    List<Alert> findByStatus(Alert.AlertStatus status);
    Page<Alert> findByStatus(Alert.AlertStatus status, Pageable pageable);

    // Find by severity
    List<Alert> findBySeverity(Alert.Severity severity);
    Page<Alert> findBySeverity(Alert.Severity severity, Pageable pageable);

    // Find by status and severity
    List<Alert> findByStatusAndSeverity(Alert.AlertStatus status, Alert.Severity severity);

    // Find active alerts (using the generated column)
    List<Alert> findByIsActiveTrue();
    Page<Alert> findByIsActiveTrue(Pageable pageable);

    // Find unacknowledged alerts
    List<Alert> findByStatusNot(Alert.AlertStatus acknowledged);

    // Find alerts within time range
    List<Alert> findByCreatedAtBetween(Instant startDate, Instant endDate);

    // Find alerts for device within time range
    List<Alert> findByDeviceIdAndCreatedAtBetween(Long deviceId, Instant startDate, Instant endDate);

    // Find alerts by type
    List<Alert> findByAlertType(String alertType);

    // Find critical alerts that are active
    List<Alert> findBySeverityAndStatusIn(Alert.Severity severity, List<Alert.AlertStatus> statuses);

    // Custom query with JOIN
    @Query("SELECT a FROM Alert a JOIN a.device d WHERE d.type.id = :typeId")
    List<Alert> findByDeviceType(@Param("typeId") Long typeId);

    // Count alerts by status
    @Query("SELECT a.status, COUNT(a) FROM Alert a GROUP BY a.status")
    List<Object[]> countByStatus();

    // Bulk acknowledge alerts
    @Modifying
    @Query("UPDATE Alert a SET a.status = 'ACKNOWLEDGED', a.acknowledgedAt = :timestamp, a.acknowledgedBy = :userId WHERE a.id IN :ids")
    int acknowledgeAlerts(@Param("ids") List<Long> alertIds, @Param("userId") User user, @Param("timestamp") Instant timestamp);

    // Find latest alerts
    @Query(value = "SELECT a FROM Alert a JOIN a.device d WHERE d.id = :deviceId ORDER BY a.createdAt DESC LIMIT :count", nativeQuery = true)
    List<Alert> findLatestAlerts(@Param("deviceId") Long deviceId, @Param("count") int count);

    // Find alerts with metadata containing specific key/value
    @Query(value = "SELECT a FROM Alert a WHERE a.additionalData @> :jsonQuery", nativeQuery = true)
    List<Alert> findByMetadata(@Param("jsonQuery") String jsonQuery);

    // Custom query using specifications
    default List<Alert> findCriticalUnacknowledgedAlerts(Alert.Severity severity,
                                                         Alert.AlertStatus status) {
        return findAll((root, query, cb) ->
                cb.and(
                        cb.equal(root.get("severity"), severity),
                        cb.notEqual(root.get("status"), status)
                )
        );
    }

    default Page<Alert> findWithFilters(
            Long deviceId,
            String alertType,
            Alert.Severity severity,
            Alert.AlertStatus status,
            Boolean isActive,
            Instant startDate,
            Instant endDate,
            Pageable pageable) {

        return findAll(createFilterSpecification(
                        deviceId, alertType, severity, status, isActive, startDate, endDate),
                pageable
        );
    }

    private Specification<Alert> createFilterSpecification(
            Long deviceId,
            String alertType,
            Alert.Severity severity,
            Alert.AlertStatus status,
            Boolean isActive,
            Instant startDate,
            Instant endDate) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (deviceId != null) {
                predicates.add(cb.equal(root.get("device").get("id"), deviceId));
            }

            if (alertType != null && !alertType.isEmpty()) {
                predicates.add(cb.equal(root.get("alertType"), alertType));
            }

            if (severity != null) {
                predicates.add(cb.equal(root.get("severity"), severity));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            if (startDate != null && endDate != null) {
                predicates.add(cb.between(root.get("createdAt"), startDate, endDate));
            } else if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            } else if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}