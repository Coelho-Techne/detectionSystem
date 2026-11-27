package com.coelhotechne.detection_system.sentinelRegistry.application;


import com.coelhotechne.detection_system.sentinelRegistry.domain.Drone;
import com.coelhotechne.detection_system.sentinelRegistry.infrastructure.DroneRepository;
import com.coelhotechne.detection_system.serverCoupling.event.SystemNodeRemovedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
public class DroneControllerRemovalListener {

    private final DroneRepository repository;

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void onNodeRemoved(SystemNodeRemovedEvent event){
        List<Drone>drones = repository.findByControllerNodeIdOrderByDroneName(event.nodeId());
        drones.forEach(Drone::decouple);
        repository.saveAll(drones);
        if (!drones.isEmpty()){
            log.info("{} drone(s) decoupled because controller {} was {}", drones.size(),event.nodeId(),event.deleted()?"deleted":"disabled");
        }
    }
}
