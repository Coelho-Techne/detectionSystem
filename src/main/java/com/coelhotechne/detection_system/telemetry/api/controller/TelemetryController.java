package com.coelhotechne.detection_system.telemetry.api.controller;

import com.coelhotechne.detection_system.telemetry.api.dto.TelemetryRequest;
import com.coelhotechne.detection_system.telemetry.api.dto.TelemetryResponse;
import com.coelhotechne.detection_system.telemetry.api.dto.TelemetrySeriesPoint;
import com.coelhotechne.detection_system.telemetry.application.TelemetryFilter;
import com.coelhotechne.detection_system.telemetry.application.TelemetryService;
import com.coelhotechne.detection_system.telemetry.domain.enums.TelemetryMetric;
import com.coelhotechne.detection_system.telemetry.exceptions.TelemetryInvalidQueryException;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Telemetry")
public class TelemetryController {
    private final TelemetryService service;
    // ---- CRUD manual ----

    @PreAuthorize(TelemetryAccess.READ)
    @GetMapping("/api/v1/telemetry")
    public ResponseEntity<Page<TelemetryResponse>>search(
            @RequestParam(name = "sensor_id",required = false)
            UUID sensorId,
            @RequestParam(name = "zone_id",required = false)
            UUID zoneId,
            @RequestParam(name = "metric",required = false)
            TelemetryMetric metric,
            @RequestParam(name = "from",required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant from,
            @RequestParam(name = "to",required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant to,
            @PageableDefault(size = 100,sort = "measuredAt",direction = Sort.Direction.DESC)
            Pageable pageable
    ){
        return ResponseEntity.ok(service.search(new TelemetryFilter(sensorId,zoneId,metric,from,to),pageable));
    }
    @PreAuthorize(TelemetryAccess.READ)
    @GetMapping(value = "/api/v1/telemetry/{id}")
    public ResponseEntity<TelemetryResponse>get(@PathVariable UUID id){
        return ResponseEntity.ok(service.get(id));
    }
    @PreAuthorize(TelemetryAccess.READ)
    @GetMapping(value = "/api/v1/sensor/{id}/telemetry/latest")
    public ResponseEntity<List<TelemetryResponse>>latest(@PathVariable UUID id){
        return ResponseEntity.ok(service.latest(id));
    }

    @PreAuthorize(TelemetryAccess.READ)
    @GetMapping(value = "/api/v1/sensor/{id}/telemetry/series")
    public ResponseEntity<List<TelemetrySeriesPoint>>series(
            @PathVariable
            UUID id,
            TelemetryMetric metric,
            @RequestParam(name = "from",required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant from,
            @RequestParam(name = "to",required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant to,
            @RequestParam(name = "bucket",defaultValue = "5m")
            String bucket
                                                            ){
        return ResponseEntity.ok(service.series(id,metric,from,to,parseBucket(bucket)));
    }

    private static Duration parseBucket(String bucket) {
        try {
            return DurationStyle.detectAndParse(bucket);
        }catch (IllegalArgumentException e){
            throw new TelemetryInvalidQueryException("invalid bucket: "+bucket);
        }
    }

    @PreAuthorize(TelemetryAccess.WRITE)
    @GetMapping(value = "/api/v1/telemetry")
    public ResponseEntity<TelemetryResponse>createManual(@Valid @RequestBody TelemetryRequest request){
        TelemetryResponse created = service.createManual(request);
        return ResponseEntity.created(URI.create("/api/v1/telemetry" + created.uuid())).body(created);
    }

    @PreAuthorize(TelemetryAccess.DELETE)
    @DeleteMapping("/api/v1/telemetry/{id}")
    public ResponseEntity<Void>delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }


}
