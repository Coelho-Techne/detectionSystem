package com.coelhotechne.detection_system.sensor.api.controller;

import com.coelhotechne.detection_system.sensor.api.dto.SensorPatchRequest;
import com.coelhotechne.detection_system.sensor.api.dto.SensorReplaceRequest;
import com.coelhotechne.detection_system.sensor.api.dto.SensorRequest;
import com.coelhotechne.detection_system.sensor.api.dto.SensorResponse;
import com.coelhotechne.detection_system.sensor.application.SensorService;
import com.coelhotechne.detection_system.sensor.domain.enums.SensorCommand;
import com.coelhotechne.detection_system.sensor.exceptions.SensorPreconditionFailedException;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Tag(name = "Sensor")
@RequestMapping(value = "/api/v1/sensor",produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class SensorController {

    private final SensorService service;

    @PreAuthorize(SensorAccess.READ)
    @GetMapping
    public ResponseEntity<Page<SensorResponse>> findSensorList(
            @PageableDefault(size = 50, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(service.findSensorPageList(pageable));
    }

    /** Devolve ETag — é daqui que o cliente tira o If-Match das escritas seguintes. */
    @PreAuthorize(SensorAccess.READ)
    @GetMapping("/{id}")
    public ResponseEntity<SensorResponse> findSensorId(@PathVariable UUID id) {
        SensorResponse sensor = service.findSensorId(id);
        return ResponseEntity.ok().eTag(etagOf(sensor)).body(sensor);
    }

    @PreAuthorize(SensorAccess.WRITE_A_T_H)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SensorResponse> createSensor(@Valid @RequestBody SensorRequest sensorRequest) {
        SensorResponse created = service.createSensor(sensorRequest);
        return ResponseEntity.status(HttpStatus.CREATED).eTag(etagOf(created)).body(created);
    }

    /** If-Match sem {@code required = false}: o 428 vem do service quando o header falta. */
    @PreAuthorize(SensorAccess.WRITE_A_T_H)
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SensorResponse> replaceSensor(
            @PathVariable UUID id,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @Valid @RequestBody SensorReplaceRequest request) {

        SensorResponse updated = service.replaceSensor(id, request, parseETag(id, ifMatch));
        return ResponseEntity.ok().eTag(etagOf(updated)).body(updated);
    }

    /**
     * Consome {@code application/json}, não {@code application/merge-patch+json}: a semântica
     * implementada é "nulo = não altera", enquanto o RFC 7396 define nulo como remoção.
     */
    @PreAuthorize(SensorAccess.WRITE_A_T_H)
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SensorResponse> patchSensor(
            @PathVariable UUID id,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @Valid @RequestBody SensorPatchRequest patch) {

        SensorResponse updated = service.patchSensor(id, patch, parseETag(id, ifMatch));
        return ResponseEntity.ok().eTag(etagOf(updated)).body(updated);
    }

    /** Transição de estado é operação, não campo de patch. */
    @PreAuthorize(SensorAccess.WRITE_A_T_H)
    @PostMapping("/{id}/maintenance")
    public ResponseEntity<SensorResponse> requestMaintenance(@PathVariable UUID id) {
        return ResponseEntity.ok(service.requestMaintenance(id));
    }

    @PreAuthorize(SensorAccess.WRITE_A_T_H)
    @DeleteMapping("/{id}/maintenance")
    public ResponseEntity<SensorResponse> clearMaintenance(@PathVariable UUID id) {
        return ResponseEntity.ok(service.clearMaintenance(id));
    }

    @PreAuthorize(SensorAccess.ONLY_ADM)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSensor(@PathVariable UUID id) {
        service.deleteSensor(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(SensorAccess.WRITE_A_T_H)
    @PostMapping("/{id}/commands/{command}")
    public ResponseEntity<Void> sendCommand(@PathVariable UUID id,
                                            @PathVariable SensorCommand command) {
        service.sendCommand(id, command);
        return ResponseEntity.accepted().build();
    }

    private static String etagOf(SensorResponse sensor) {
        return "\"" + sensor.version() + "\"";
    }

    /**
     * Header ausente ou {@code *} devolve null — no PUT o service transforma isso em 428.
     * Valor não numérico vira 412: é ETag sintaticamente válido, só não é um dos nossos.
     */
    private static Long parseETag(UUID id, String ifMatch) {
        if (ifMatch == null || ifMatch.isBlank() || "*".equals(ifMatch.trim())) {
            return null;
        }
        String raw = ifMatch.replace("W/", "").replace("\"", "").trim();
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            throw new SensorPreconditionFailedException(id.toString(), raw, null);
        }
    }
}
