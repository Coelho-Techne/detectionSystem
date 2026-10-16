package com.coelhotechne.detection_system.telemetry.application;

import com.coelhotechne.detection_system.sensor.application.SensorService;
import com.coelhotechne.detection_system.sensor.domain.Sensor;
import com.coelhotechne.detection_system.telemetry.api.dto.TelemetryMapper;
import com.coelhotechne.detection_system.telemetry.api.dto.TelemetryRequest;
import com.coelhotechne.detection_system.telemetry.api.dto.TelemetryResponse;
import com.coelhotechne.detection_system.telemetry.api.dto.TelemetrySeriesPoint;
import com.coelhotechne.detection_system.telemetry.domain.Telemetry;
import com.coelhotechne.detection_system.telemetry.domain.enums.TelemetryMetric;
import com.coelhotechne.detection_system.telemetry.domain.enums.TelemetrySource;
import com.coelhotechne.detection_system.telemetry.exceptions.TelemetryInvalidQueryException;
import com.coelhotechne.detection_system.telemetry.exceptions.TelemetryNotFoundException;
import com.coelhotechne.detection_system.telemetry.exceptions.TelemetryRejectedException;
import com.coelhotechne.detection_system.telemetry.infrastructure.TelemetryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Service
@Log4j2
@RequiredArgsConstructor
public class TelemetryServiceImpl implements TelemetryService{

    static final Duration MIN_BUCKET = Duration.ofMinutes(1);
    static final Duration MAX_BUCKET = Duration.ofDays(1);
    private final TelemetryRepository repository;
    private final TelemetryMapper mapper;
    private final SensorService sensorService;
    private final TelemetryThresholdEvaluator thresholdEvaluator;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public Page<TelemetryResponse>search(TelemetryFilter filter, Pageable pageable){
        TelemetryFilter bounded = filter.withDefaults(clock.instant());
        return repository.findAll(TelemetrySpecifications.of(bounded),pageable).map(mapper::toResponse);
    }

    @Override
    public TelemetryResponse get(UUID id) {
        return mapper.toResponse(requireId(id));

    }

    @Override
    public List<TelemetryResponse> latest(UUID sensorId) {
        sensorService.requireSensor(sensorId);
        return repository
                .findLatestPerMetric(sensorId)
                .stream().map(mapper::toResponse)
                .toList();
    }

    @Override
    public List<TelemetrySeriesPoint> series(UUID sensorId, TelemetryMetric metric, Instant from, Instant to, Duration bucket) {
        if (metric==null){
            throw new TelemetryInvalidQueryException("'metric' is required");
        }
        if (bucket==null|| bucket.compareTo(MIN_BUCKET)<0|| bucket.compareTo(MAX_BUCKET)>0){
            throw new TelemetryInvalidQueryException("'bucket' must be between 1m and 1d");
        }
        sensorService.requireSensor(sensorId);
        TelemetryFilter filter = new TelemetryFilter(sensorId,null,metric,from,to).withDefaults(clock.instant());
        return repository.findSeries(
                sensorId,metric.name(), filter.from(),filter.to(),bucket.toSeconds()
        ).stream().map(row -> new TelemetrySeriesPoint(
                Instant.ofEpochSecond(row.getBucketStart().longValue()),
                toDecimal(row.getAvgValue()),
                toDecimal(row.getMinValue()),
                toDecimal(row.getMaxValue()),
                row.getSamples().longValue())).toList();
    }

    @Override
    public TelemetryResponse createManual(TelemetryRequest request) {
        Sensor sensor = sensorService.requireSensor(request.sensorId());
        TelemetryMetric metric = TelemetryMetric.fromCode(request.metric());
        if (metric == null){
            throw new TelemetryRejectedException(request.sensorId()," unknown metric '%s'".formatted(request.metric()));
        }
        if (!metric.allowsNiche(sensor.getSensorNiche())){
            throw new TelemetryRejectedException(request.sensorId(),
                    "metric %s not allowed for niche %s".formatted(metric,sensor.getSensorNiche()));
        }
        Instant now = clock.instant();
        Instant measuredAt = request.measuredAt()!=null? request.measuredAt():now;
        Telemetry saved = repository.save(new Telemetry(
                sensor,
                metric,
                request.value(),
                measuredAt,
                now,
                TelemetrySource.MANUAL,
                null));
                thresholdEvaluator.evaluete(List.of(saved));
            log.info("Telemetry {} ({}) created manually for sensor {}",saved.getUuid(),metric,sensor.getUuid());

            return mapper.toResponse(saved);
    }

    @Override
    public void delete(UUID uuid) {
        repository.delete(requireId(uuid));
        log.info("Telemetry {} deleted manually",uuid);
    }
    private static BigDecimal toDecimal(Number n){
        if (n == null){
            return null;
        }
        return n instanceof  BigDecimal bigDecimal ? bigDecimal: new BigDecimal(n.toString());
    }
    private Telemetry requireId(UUID uuid){
        return repository.findById(uuid).orElseThrow(()-> new TelemetryNotFoundException(uuid.toString()," Telemetry not found! "));
    }

}
