package com.coelhotechne.detection_system.sentinelRegistry.application;


import com.coelhotechne.detection_system.globalClass.security.AccessKeyHasher;
import com.coelhotechne.detection_system.sentinelRegistry.api.dto.DroneMapper;
import com.coelhotechne.detection_system.sentinelRegistry.api.dto.link.*;
import com.coelhotechne.detection_system.sentinelRegistry.config.DroneLinkProperties;
import com.coelhotechne.detection_system.sentinelRegistry.domain.Drone;
import com.coelhotechne.detection_system.sentinelRegistry.domain.DroneFlightEvent;
import com.coelhotechne.detection_system.sentinelRegistry.domain.DroneLiveState;
import com.coelhotechne.detection_system.sentinelRegistry.domain.DroneStatusReport;
import com.coelhotechne.detection_system.sentinelRegistry.event.DroneFlightEventRecorded;
import com.coelhotechne.detection_system.sentinelRegistry.exceptions.DroneInactiveException;
import com.coelhotechne.detection_system.sentinelRegistry.exceptions.DroneLinkException;
import com.coelhotechne.detection_system.sentinelRegistry.infrastructure.DroneFlightEventRepository;
import com.coelhotechne.detection_system.sentinelRegistry.infrastructure.DroneLiveStateRepository;
import com.coelhotechne.detection_system.sentinelRegistry.infrastructure.DroneRepository;
import com.coelhotechne.detection_system.sentinelRegistry.infrastructure.DroneStatusReportRepository;
import com.coelhotechne.detection_system.serverCoupling.application.SystemNodePrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

@Log4j2
@Service
@RequiredArgsConstructor
public class DroneLinkService {

    private final Clock clock;
    private final DroneMapper mapper;
    private final DroneRegistry registry;
    private final DroneRepository repository;
    private final DroneLinkProperties properties;
    private final AccessKeyHasher accessKeyHasher;
    private final ApplicationEventPublisher eventPublisher;
    private final DroneLiveStateRepository liveStateRepository;
    private final DroneFlightEventRepository flightEventRepository;
    private final DroneStatusReportRepository statusReportRepository;

    public CoupledDroneResponse couple(SystemNodePrincipal controller, UUID droneId, String droneAccessKey){
        Drone drone =  registry.requireDrone(droneId);
        if (!accessKeyHasher.matches(droneAccessKey,drone.getAccessKey())){
            log.warn("Controller {} failed to couple drone {}: invalid drone key",controller.nodeIdentifier(),droneId);
            throw DroneLinkException.invalidDroneKey(droneId);
        }
        if (!drone.isActive()){
            throw new DroneInactiveException(droneId);
        }
        if (drone.isCoupled() && !drone.isCoupledTo(controller.nodeId())){
            throw DroneLinkException.coupledElsewhere(droneId);
        }
        Instant now = clock.instant();
        boolean already = drone.isCoupledTo(controller.nodeId());
        drone.setLastCommunication(LocalDateTime.now(clock));
        Drone saved = repository.saveAndFlush(drone);
        DroneLiveState liveState = liveStateRepository.findById(droneId).orElseGet(()-> new DroneLiveState(droneId,controller.nodeId()));
        liveState.assignController(controller.nodeId());
        liveStateRepository.save(liveState);
        if (!already){
            log.info("Drone {} ({}) coupled to controller {}",droneId,saved.getSerialNumber(),controller.nodeIdentifier());
        }
        return CoupledDroneResponse.of(mapper.toRegistered(saved));
    }
    @Transactional
    public void decouple(SystemNodePrincipal controller, UUID droneId){
        Drone drone = requireCoupled(controller, droneId);
        drone.decouple();
        repository.saveAndFlush(drone);
        log.info("Drone {} decoupled by controller {}",droneId,controller.nodeIdentifier());
    }

    @Transactional(readOnly = true)
    public List<CoupledDroneResponse>myDrones(SystemNodePrincipal controller){
        return repository.findByControllerNodeIdOrderByDroneName(controller.nodeId()).stream()
                .map(mapper::toRegistered).map(CoupledDroneResponse::of).toList();
    }
    @Transactional(readOnly = true)
    public CoupledDroneResponse myDrone(SystemNodePrincipal controller, UUID droneId){
        return CoupledDroneResponse.of(mapper.toRegistered(requireCoupled(controller,droneId)));
    }
    @Transactional
    public DroneIngestResponse ingestReports(SystemNodePrincipal controller, UUID droneId, DroneStatusBatchRequest batchRequest){
        Drone drone = requireCoupled(controller,droneId);
        Instant now = clock.instant();
        List<DroneStatusReportItem>items = batchRequest.reports();

        for (int i =0; i<items.size();i++){
            checkWindow(droneId,"reports",i,items.get(i).recordedAt(),now);
        }
        List<DroneStatusReportItem> fresh = dedup(items, DroneStatusReportItem::reportId,
                ids -> statusReportRepository.findExistingReportsIds(droneId, ids));
        List<DroneStatusReport> saved = statusReportRepository.saveAll(fresh.stream()
                .map(r -> new DroneStatusReport(droneId,
                        controller.nodeId(),
                        r.reportId(),
                        r.recordedAt(), now,
                        r.flightState(),
                        r.latitude(),
                        r.longitude(), r.altitudeMeters(),
                        r.headingDegrees(),
                        r.groundSpeedMs(),
                        r.batteryPercent(),
                        r.batteryVoltage(),
                        r.linkQualityPercent(),
                        r.rssiDbm(),
                        r.satellites(),
                        trimToNull(r.missionId())))
                .toList());

        if (!saved.isEmpty()){
            DroneLiveState live = liveState(droneId,controller);
            saved.stream().max(Comparator.comparing(DroneStatusReport::getRecordedAt)).ifPresent(live::apply);
            liveStateRepository.save(live);
        }

        repository.touchLastCommunication(drone.getUuid(),LocalDateTime.now(clock));
        return new DroneIngestResponse(droneId,saved.size(),items.size() - saved.size(),now);
    }
    @Transactional
    public DroneIngestResponse ingestEvents(SystemNodePrincipal controller, UUID droneId, DroneEventBatchRequest batchRequest){
        Drone drone =requireCoupled(controller,droneId);
        Instant now = clock.instant();
        List<DroneFlightEventItem>items = batchRequest.events();
        for (int i = 0; i<items.size();i++){
            DroneFlightEventItem e = items.get(i);
            checkWindow(droneId,"events",i,e.occurredAt(),now);
            if (e.command() != null && !e.eventType().isCommandAudit()){
                throw DroneLinkException.outOfWindow(droneId,"events",i,"command is only allowed on COMMAND_* events");
            }
        }

        List<DroneFlightEventItem> fresh = dedup(items,DroneFlightEventItem::eventId,
                ids -> flightEventRepository.findExistingEventIds(droneId,ids));
        List<DroneFlightEvent>saved = flightEventRepository.saveAll(fresh.stream()
                .sorted(Comparator.comparing(DroneFlightEventItem::occurredAt))
                .map(e -> new DroneFlightEvent(droneId,
                        controller.nodeId(),
                        e.eventId(),
                        e.eventType(),
                        e.severity(),
                        e.occurredAt(),
                        now,
                        trimToNull(e.missionId()),
                        e.command(),
                        trimToNull(e.message()),
                        e.latitude(),
                        e.longitude()))
                        .toList());

        if (!saved.isEmpty()){
            DroneLiveState live = liveState(droneId,controller);
            saved.forEach(live::apply);
            liveStateRepository.save(live);
            for (DroneFlightEvent e : saved){
                eventPublisher.publishEvent(new DroneFlightEventRecorded(droneId,
                        drone.getDroneName(),
                        controller.nodeId(),
                        e.getEventType(),
                        e.getSeverity(),
                        e.getMessage(),
                        e.getOccurredAt()));
            }
        }
        repository.touchLastCommunication(drone.getUuid(),LocalDateTime.now(clock));
        return new DroneIngestResponse(droneId,saved.size(),items.size() - saved.size(),now);
    }



    //::::::::::::::::::::::::::::::::::::::::::::::: Until :::::::::::::::::::::::::::::::::::::::::::::::

    private Drone requireCoupled(SystemNodePrincipal controller, UUID droneId){
        Drone drone = registry.requireDrone(droneId);
        if (!drone.isCoupledTo(controller.nodeId())){
            throw DroneLinkException.notCoupled(droneId);
        }
        return drone;
    }

    private DroneLiveState liveState(UUID droneId,SystemNodePrincipal controller){
        return liveStateRepository.findById(droneId).orElseGet(()-> new DroneLiveState(droneId,controller.nodeId()));
    }

    private void checkWindow(UUID droneId,String field, int index,Instant at,Instant now){
        if (at.isAfter(now.plus(properties.maxBackfill()))){
            throw DroneLinkException.outOfWindow(droneId,field,index,"timestamp is more than "+ properties.maxClockSkew()+" in the future(check the controller clock)");
        }

        if (at.isBefore(now.minus(properties.maxBackfill()))){
            throw DroneLinkException.outOfWindow(droneId,field,index,"timestamp is older than"+properties.maxClockSkew());
        }

    }

    private static <T> List<T> dedup(List<T> items, Function<T,UUID>idOf, Function<Set<UUID>,List<UUID>>existing){
        Map<UUID,T>unique = new LinkedHashMap<>();
        for (T item : items){
            unique.putIfAbsent(idOf.apply(item),item);
        }
        Set<UUID> already = new HashSet<>((existing.apply(unique.keySet())));
        return unique.values().stream().filter(item->!already.contains(idOf.apply(item))).toList();
    }

    private static String trimToNull(String value){
        if (value == null){
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
