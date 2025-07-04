package com.smartsolutions.smart_solutions.service;

import com.smartsolutions.smart_solutions.dto.SensorDataDTO;
import com.smartsolutions.smart_solutions.util.SensorDataMapper;
import com.smartsolutions.smart_solutions.model.SensorData;
import com.smartsolutions.smart_solutions.repository.SensorDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SensorDataService {

    private final SensorDataRepository sensorDataRepository;
    private final SensorDataMapper sensorDataMapper;

    @Transactional(readOnly = true)
    public List<SensorDataDTO> getLatestReadings(int count) {
        return sensorDataRepository.findLatestReadings(count).stream()
                .map(sensorDataMapper::toDto)
                .collect(Collectors.toList());
    }
}