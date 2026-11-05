package com.coelhotechne.detection_system.sensor.application.auth;

import com.coelhotechne.detection_system.globalClass.security.AccessKeyGenerator;
import com.coelhotechne.detection_system.globalClass.security.IssuedAccessKeyResponse;
import com.coelhotechne.detection_system.sensor.application.SensorService;
import com.coelhotechne.detection_system.sensor.domain.Sensor;
import com.coelhotechne.detection_system.sensor.exceptions.SensorAuthenticationException;
import com.coelhotechne.detection_system.sensor.infrastructure.SensorRepository;
import com.coelhotechne.detection_system.sensor.security.SensorAccessKeyHasher;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Log4j2
@AllArgsConstructor
public class SensorAuthServiceImpl implements SensorAuthService {
    private final SensorRepository repository;
    private final SensorAccessKeyHasher hasher;
    private final SensorService service;
    private final AccessKeyGenerator generator;
    private final Clock clock;

    @Override
    @Transactional
    public Optional<Sensor> authenticate(UUID sensorId, String rawAccessKey) {
        return repository.findById(sensorId)
                .filter(sensor -> hasher.matches(rawAccessKey, sensor.getAccessKey()))
                .map(sensor -> {
                    sensor.setLastCommunication(clock.instant());
                    return repository.save(sensor);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public void requireValidAccessKey(UUID sensorId, String rawAccessKey) {
        boolean valid = repository.findById(sensorId)
                .map(sensor -> hasher.matches(rawAccessKey, sensor.getAccessKey()))
                .orElse(false);
        if (!valid) {
            throw new SensorAuthenticationException(sensorId.toString(),
                    "Access key invalid or Sensor does not exist");
        }
    }

    @Override
    @Transactional
    public IssuedAccessKeyResponse issueAccessKey(UUID sensorId) {
        Sensor sensor = service.requireSensor(sensorId);
        boolean rotation = sensor.getAccessKey() != null;
        String rawKey = generator.generate();
        sensor.setAccessKey(hasher.hash(rawKey));
        Instant now = clock.instant();
        log.info("Access key {} for sensor {}",rotation ? "rotated" : "issued",sensorId);
        return new IssuedAccessKeyResponse(sensorId,rawKey,now);
    }
}
