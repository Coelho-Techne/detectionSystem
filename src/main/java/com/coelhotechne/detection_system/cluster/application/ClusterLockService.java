package com.coelhotechne.detection_system.cluster.application;

import com.coelhotechne.detection_system.cluster.config.ClusterProperties;
import com.coelhotechne.detection_system.cluster.infrastructure.ClusterLeaseRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Log4j2
@Service
public class ClusterLockService {
    private final ClusterLeaseRepository repository;
    private final ClusterProperties properties;
    private final TransactionTemplate template;
    private final Clock clock;

    public ClusterLockService(ClusterLeaseRepository repository,
                              ClusterProperties properties,
                              PlatformTransactionManager transactionManager,
                              Clock clock) {
        this.repository = repository;
        this.properties = properties;
        this.clock = clock;
        this.template = new TransactionTemplate(transactionManager);
        this.template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public boolean runExclusively(String name, Duration lockMostFor, Duration lockAtLeastFor,Runnable task){
        if (!properties.isEnabled()){
            task.run();
            return true;
        }
        Instant start = clock.instant();
        if (!acquire(name,start,start.plus(lockMostFor))){
            log.debug("Task '{}' skipped: lease held by another node",name);
            return false;
        }
        try {
            task.run();
            return true;
        }finally {
            Instant releaseAt = later(clock.instant(),start.plus(lockAtLeastFor));
            try {
                template.executeWithoutResult(s -> repository.release(name,properties.nodeId(),releaseAt));
            }catch (RuntimeException ex){
                log.warn("Failed to release lease '{}'; it expires by itself at {}",name,start.plus(lockMostFor),ex);
            }
        }
    }

    boolean acquire(String name, Instant now,Instant until){
        String owner = properties.nodeId();
        Integer taken = template.execute( s -> repository.tryTake(name,owner,now,until));
        if (taken != null && taken == 1){
            return true;
        }
        Boolean exists = template.execute(s -> repository.existsById(name));
        if (Boolean.TRUE.equals(exists)){
            return  false;
        }
        try{
            template.executeWithoutResult(s -> repository.insertNew(name,owner,now,until));
            return true;
        }catch (DataIntegrityViolationException raced){
            return false;
        }
    }

    private static Instant later(Instant a, Instant b){
        return a.isAfter(b) ? a : b;
    }
}
