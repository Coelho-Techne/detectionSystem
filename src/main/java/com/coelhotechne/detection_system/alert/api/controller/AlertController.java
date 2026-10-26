package com.coelhotechne.detection_system.alert.api.controller;

import com.coelhotechne.detection_system.alert.api.dto.AlertResolveRequest;
import com.coelhotechne.detection_system.alert.api.dto.AlertResponse;
import com.coelhotechne.detection_system.alert.api.dto.AlertSummaryResponse;
import com.coelhotechne.detection_system.alert.application.AlertFilter;
import com.coelhotechne.detection_system.alert.application.AlertService;
import com.coelhotechne.detection_system.alert.domain.enums.AlertCategory;
import com.coelhotechne.detection_system.alert.domain.enums.AlertStatus;
import com.coelhotechne.detection_system.alert.domain.enums.Severity;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@Tag(name = "Alert")
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/alert",produces = MediaType.APPLICATION_JSON_VALUE)
public class AlertController {
    private final AlertService alertService;

    @PreAuthorize(AlertAccess.READ)
    @GetMapping
    public ResponseEntity<Page<AlertResponse>>search(
            @RequestParam(name = "status", required = false)
            AlertStatus status,
            @RequestParam(name = "severity", required = false)
            Severity severity,
            @RequestParam(name = "category", required = false)
            AlertCategory category,
            @RequestParam(name = "zone_id", required = false)
            UUID zoneId,
            @RequestParam(name = "source_id", required = false)
            UUID sourceId,
            @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant from,
            @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant to,
            @PageableDefault(size = 50, sort = "lastOccurredAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ){
        AlertFilter filter = new AlertFilter(status,severity,category,zoneId,sourceId,from,to);
        return ResponseEntity.ok(alertService.search(filter,pageable));
    }
    @PreAuthorize(AlertAccess.READ)
    @GetMapping("/summary")
    public ResponseEntity<AlertSummaryResponse>summary(){
        return ResponseEntity.ok(alertService.summary());
    }
    @PreAuthorize(AlertAccess.READ)
    @GetMapping(value = "/{id}")
    public ResponseEntity<AlertResponse>get(@PathVariable UUID id){
        AlertResponse alertResponse = alertService.get(id);
        return ResponseEntity.ok().eTag(etagOf(alertResponse)).body(alertResponse);
    }
    @PreAuthorize(AlertAccess.HANDLE)
    @GetMapping("/{id}/acknowledge")
    public ResponseEntity<AlertResponse>acknowledge(
            @PathVariable
            UUID id,
            @RequestHeader(value = HttpHeaders.IF_MATCH,required = false)
            String ifMatch,
            Authentication authentication
    ){
        AlertResponse alertResponse = alertService.acknowledge(id,authentication.getName(),versionOf(ifMatch));
        return ResponseEntity.ok().eTag(etagOf(alertResponse)).body(alertResponse);
    }

    @PreAuthorize(AlertAccess.HANDLE)
    @PostMapping(value = "/{id}/resolve",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AlertResponse>resolve(
            @PathVariable
            UUID id,
            @Valid@RequestBody
            AlertResolveRequest alertResolveRequest,
            @RequestHeader(value = HttpHeaders.IF_MATCH,required = false)
            String ifMatch,
            Authentication authentication
    ){
        AlertResponse alertResponse=alertService.resolve(id,authentication.getName(),alertResolveRequest.note(),versionOf(ifMatch));
        return ResponseEntity.ok().eTag(etagOf(alertResponse)).body(alertResponse);
    }

    private static String etagOf(AlertResponse alertResponse){
        return "\""+alertResponse.version()+"\"";
    }

    private static Long versionOf(String ifMatch){
        if (ifMatch == null||ifMatch.isBlank()){
            return null;
        }
        String raw = ifMatch.trim().replaceFirst("^W/","").replace("\"","");

        try {
            return Long.parseLong(raw);
        }catch (NumberFormatException ex){
            return null;
        }
    }
}
