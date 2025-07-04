package com.smartsolutions.smart_solutions.service;

import com.smartsolutions.smart_solutions.dto.DeviceDTO;
import com.smartsolutions.smart_solutions.dto.SensorDataDTO;
import com.smartsolutions.smart_solutions.model.Alert;
import com.smartsolutions.smart_solutions.model.Device;
import com.smartsolutions.smart_solutions.model.SensorData;
import com.smartsolutions.smart_solutions.repository.DeviceRepository;
import com.smartsolutions.smart_solutions.repository.SensorDataRepository;
import com.smartsolutions.smart_solutions.util.DeviceMapper;
import com.smartsolutions.smart_solutions.util.SensorDataMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final SensorDataRepository sensorDataRepository;
    private final SensorDataMapper sensorDataMapper;
    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;

    @Transactional(readOnly = true)
    public Page<SensorDataDTO> getDeviceSensorData(
            Long deviceId, String sensorType, Instant startDate, Instant endDate, Pageable pageable) {

        validateDeviceExists(deviceId);

        Page<SensorData> dataPage;
        if (sensorType != null && startDate != null && endDate != null) {
            dataPage = sensorDataRepository.findByDeviceIdAndSensorTypeAndTimestampBetween(
                    deviceId, sensorType, startDate, endDate, pageable);
        } else if (sensorType != null) {
            dataPage = sensorDataRepository.findByDeviceIdAndSensorType(deviceId, sensorType, pageable);
        } else if (startDate != null && endDate != null) {
            dataPage = sensorDataRepository.findByDeviceIdAndTimestampBetween(
                    deviceId, startDate, endDate, pageable);
        } else {
            dataPage = sensorDataRepository.findByDeviceId(deviceId, pageable);
        }

        return dataPage.map(sensorDataMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<String> getDeviceSensorTypes(Long deviceId) {
        return sensorDataRepository.findDistinctSensorTypesByDeviceId(deviceId);
    }

    @Transactional
    public SensorDataDTO addReading(Long deviceId, SensorDataDTO sensorDataDTO) {
        SensorData sensorData = sensorDataMapper.toEntity(sensorDataDTO);
        // Set device references
        if (deviceId != null) {
            Device device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new EntityNotFoundException("Device not found with id: " + deviceId));
            sensorData.setDevice(device);
        }
        SensorData savedData = sensorDataRepository.save(sensorData);
        return sensorDataMapper.toDto(savedData);
    }

    @Transactional(readOnly = true)
    public void validateDeviceExists(Long deviceId) {
        if (deviceId == null) {
            throw new IllegalArgumentException("Device ID cannot be null");
        }
        if (!deviceRepository.existsById(deviceId)) {
            throw new EntityNotFoundException(
                    String.format("Device with ID %d not found", deviceId));
        }
    }


        @Transactional(readOnly = true)
        public Page<DeviceDTO> getAllDevices(Pageable pageable) {
            return deviceRepository.findAll(pageable)
                    .map(deviceMapper::toDto);
        }

        @Transactional(readOnly = true)
        public DeviceDTO getDeviceById(Long id) {
            return deviceRepository.findById(id)
                    .map(deviceMapper::toDto)
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format("Device with ID %d not found", id)));
        }

        @Transactional
        public Device createDevice(Device device) {
            return deviceRepository.save(device);
        }

        @Transactional
        public DeviceDTO updateDevice(Long id, DeviceDTO deviceDTO) {
            Device existingDevice = deviceRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format("Device with ID %d not found", id)));

            deviceMapper.updateEntityFromDto(deviceDTO, existingDevice);
            existingDevice.setUpdatedAt(Instant.now());
            return deviceMapper.toDto(deviceRepository.save(existingDevice));
        }

        @Transactional
        public void deleteDevice(Long id) {
            Device device = deviceRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format("Device with ID %d not found", id)));

            deviceRepository.delete(device);
        }
}
