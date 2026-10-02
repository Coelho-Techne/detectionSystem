package com.coelhotechne.detection_system.sensor.application.homologation;

import com.coelhotechne.detection_system.sensor.domain.homologation.SensorHomologationRecord;
import com.coelhotechne.detection_system.sensor.infrastructure.SensorRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Log4j2
@Service
@AllArgsConstructor
public class SensorHomologationServiceImpl implements SensorHomologationService{
    private final SensorRepository repository;
    @Override
    public SensorHomologationRecord runConnectionTest(UUID camId) {
        return null;
    }

    @Override
    public SensorHomologationRecord approve(UUID camId, String approvedBy) {
        return null;
    }

    @Override
    public SensorHomologationRecord reject(UUID camId, String rejectedBy, String reason) {
        return null;
    }

    @Override
    public boolean expireIfOverdue(UUID sensorId, Duration timeout, Instant now) {
        return repository.findById(sensorId)
                .map(sensor -> sensor.getHomologation().expireIfOverdue(now, timeout))
                .orElse(false);
    }
}
