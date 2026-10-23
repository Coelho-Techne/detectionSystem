package com.coelhotechne.detection_system.alert.application;

import com.coelhotechne.detection_system.alert.api.dto.AlertMapper;
import com.coelhotechne.detection_system.alert.api.dto.AlertResponse;
import com.coelhotechne.detection_system.alert.api.dto.AlertSummaryResponse;
import com.coelhotechne.detection_system.alert.domain.Alert;
import com.coelhotechne.detection_system.alert.domain.enums.AlertStatus;
import com.coelhotechne.detection_system.alert.event.AlertChangedEvent;
import com.coelhotechne.detection_system.alert.exceptions.AlertNotFoundException;
import com.coelhotechne.detection_system.alert.exceptions.AlertPreconditionFailedException;
import com.coelhotechne.detection_system.alert.exceptions.AlertResolutionNoteRequiredException;
import com.coelhotechne.detection_system.alert.exceptions.AlertTransitionNotAllowedException;
import com.coelhotechne.detection_system.alert.infrastructure.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final AlertRepository repository;
    private final AlertMapper mapper;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public Page<AlertResponse> search(AlertFilter filter, Pageable pageable) {
        return repository.findAll(AlertSpecifications.of(filter),pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AlertResponse get(UUID id) {
        return mapper.toResponse(requireId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public AlertSummaryResponse summary() {
        return mapper.toSummary(
                repository.countOpenBySeverity(AlertStatus.OPEN),repository.countOpenByZone(AlertStatus.OPEN)
        );
    }

    @Override
    @Transactional
    public AlertResponse acknowledge(UUID id, String user, Long expectedVersion) {
        Alert alert = requireId(id);
        requireVersion(alert,expectedVersion);
        try {
            alert.acknowledge(user,clock.instant());
        }catch (IllegalArgumentException e){
            throw new AlertTransitionNotAllowedException(id,alert.getStatus(),"acknowledge");
        }
        Alert saved = repository.saveAndFlush(alert);
        log.info("Alert {} acknowledge by {}",id,user);
        publish(saved, AlertChangedEvent.Change.ACKNOWLEDGE);
        return mapper.toResponse(saved);
    }

    @Override
    public AlertResponse resolve(UUID id, String user, String note, Long expectedVersion) {
        Alert alert = requireId(id);
        requireVersion(alert, expectedVersion);
        if (!alert.getStatus().isOpen()) {
            throw new AlertTransitionNotAllowedException(id, alert.getStatus(), "resolve");
        }
        try {
            alert.resolve(user, note, clock.instant());
        } catch (IllegalArgumentException ex) {
            throw new AlertResolutionNoteRequiredException(id);
        }
        Alert saved = repository.saveAndFlush(alert);
        log.info("Alert {} resolved by {}", id, user);
        publish(saved, AlertChangedEvent.Change.RESOLVED);
        return mapper.toResponse(saved);
    }
    private Alert requireId(UUID uuid){
        return repository.findById(uuid).orElseThrow(()->new AlertNotFoundException(uuid));
    }

    private static void requireVersion(Alert alert,Long expectedVersion){
        if (expectedVersion!=null&& !expectedVersion.equals(alert.getVersion())){
            throw new AlertPreconditionFailedException(alert.getUuid(),expectedVersion,alert.getVersion());
        }
    }

    private void  publish(Alert alert,AlertChangedEvent.Change change){
        eventPublisher.publishEvent(new AlertChangedEvent(alert.getUuid(),
                change,
                alert.getStatus(),
                alert.getSeverity(),
                alert.getZone() == null ? null: alert.getZone().getUuid()));
    }
}
