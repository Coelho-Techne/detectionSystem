package com.coelhotechne.detection_system.sentinelRegistry.application;

import com.coelhotechne.detection_system.sentinelRegistry.api.dto.link.DroneFlightEventResponse;
import com.coelhotechne.detection_system.sentinelRegistry.api.dto.link.DroneLiveStateResponse;
import com.coelhotechne.detection_system.sentinelRegistry.api.dto.link.DroneStatusReportResponse;
import com.coelhotechne.detection_system.sentinelRegistry.domain.Drone;
import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DroneEventType;
import com.coelhotechne.detection_system.sentinelRegistry.exceptions.DroneNotFoundException;
import com.coelhotechne.detection_system.sentinelRegistry.infrastructure.DroneFlightEventRepository;
import com.coelhotechne.detection_system.sentinelRegistry.infrastructure.DroneLiveStateRepository;
import com.coelhotechne.detection_system.sentinelRegistry.infrastructure.DroneRepository;
import com.coelhotechne.detection_system.sentinelRegistry.infrastructure.DroneStatusReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class DroneTelemetryQueryService {

    static final Duration DEFAULT_WINDOW = Duration.ofHours(24);
    static final Duration MAX_WINDOW = Duration.ofDays(31);

    private final Clock clock;
    private final DroneRepository droneRepository;
    private final DroneFlightEventRepository eventRepository;
    private final DroneStatusReportRepository reportRepository;
    private final DroneLiveStateRepository liveStateRepository;

    @Transactional(readOnly = true)
    public DroneLiveStateResponse live(UUID droneId){
        Drone drone = requireDrone(droneId);
        return DroneLiveStateResponse.of(drone,liveStateRepository.findById(droneId).orElse(null));
    }
    @Transactional(readOnly = true)
    public Page<DroneStatusReportResponse>reports(UUID droneId, Instant from, Instant to, Pageable pageable){
        requireDrone(droneId);
        Instant[] w = window(from,to);
        return reportRepository.findWindow(droneId,w[0],w[1],pageable).map(DroneStatusReportResponse::of);
    }
    public Page<DroneFlightEventResponse>events(UUID droneId, Set<DroneEventType>types, Instant from, Instant to, Pageable pageable){
        requireDrone(droneId);
        Instant[] w = window(from,to);
        Collection<DroneEventType> filter = types == null || types.isEmpty() ?
                EnumSet.allOf(DroneEventType.class) : types;
        return eventRepository.findWindow(droneId,filter,w[0],w[1], pageable).map(DroneFlightEventResponse::of);
    }
    @Transactional
    public DroneLiveStateResponse forceDecouple(UUID droneId){
        Drone drone = requireDrone(droneId);
        if (drone.isCoupled()){
            log.info("Drone {} decoupled from controller {} by an operator",
                    droneId, drone.getControllerNodeId());
            drone.decouple();
            droneRepository.saveAndFlush(drone);
        }
        return DroneLiveStateResponse.of(drone,liveStateRepository.findById(droneId).orElse(null));
    }

    //:::::::::::::::::::::::::::: Until ::::::::::::::::::::::::::::::::::::

    private Drone requireDrone(UUID droneId){
        return droneRepository.findById(droneId).orElseThrow(()-> new DroneNotFoundException(droneId));
    }

    private Instant[] window(Instant from, Instant to){
        Instant end = to != null ? to : clock.instant();
        Instant start = from != null ? from : end.minus(DEFAULT_WINDOW);
        if (!start.isBefore(end)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"from must be before to");
        }
        if (Duration.between(start,end).compareTo(MAX_WINDOW)>0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"window must be at most "+MAX_WINDOW.toDays()+" days");
        }
        return new Instant[]{start,end};
    }
}
