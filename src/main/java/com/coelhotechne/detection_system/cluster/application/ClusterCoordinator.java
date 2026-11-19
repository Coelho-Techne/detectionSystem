package com.coelhotechne.detection_system.cluster.application;

import com.coelhotechne.detection_system.cluster.config.ClusterProperties;
import com.coelhotechne.detection_system.cluster.domain.ServerClusterNode;
import com.coelhotechne.detection_system.cluster.domain.enums.NodeStatus;
import com.coelhotechne.detection_system.cluster.infrastructure.ServerNodeRepository;
import jakarta.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.InetAddress;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

@Log4j2
@Service
public class ClusterCoordinator {

    private final ServerNodeRepository repository;
    private final ClusterProperties properties;
    private final TaskScheduler scheduler;
    private final TransactionTemplate tx;
    private final Clock clock;


    private volatile ScheduledFuture<?> heartbeatTask;
    private volatile Instant lastSuccessfulHeartbeat;

    public ClusterCoordinator(ServerNodeRepository repository,
                              ClusterProperties properties,
                              TaskScheduler scheduler,
                              PlatformTransactionManager transactionManager,
                              Clock clock){
        this.repository = repository;
        this.properties = properties;
        this.scheduler = scheduler;
        this.tx = new TransactionTemplate(transactionManager);
        this.clock = clock;
    }

    public void start(){
        if (!properties.isEnabled()){
            log.info("Cluster disabled (cluster.enabled=false): running as a single instance");
            return;
        }
        heartbeat();
        heartbeatTask = scheduler.scheduleWithFixedDelay(this::heartbeat, properties.heartbeatInterval());
        log.info("Cluster node '{}' started (heartbeat {}, timeout {})",properties.nodeId(),properties.heartbeatInterval(),properties.nodeTimeout());
    }

    void heartbeat(){
        try {
            tx.executeWithoutResult(s->{
                Instant now = clock.instant();
                ServerClusterNode self = repository.findById(properties.nodeId()).orElse(null);
                if (self == null){
                    repository.save(new ServerClusterNode(properties.nodeId(), hostname(),now));
                }else if (self.getStatus() != NodeStatus.UP){
                    self.restart(hostname(),now);
                }else {
                    self.heartbeat(now);
                }
                int stale = repository.markStale(now.minus(properties.nodeTimeout()),NodeStatus.UP,NodeStatus.DOWN);
                if (stale>0){
                    log.warn("Cluster: {} node(s) without heartbeat for {} marked DOWN", stale, properties.nodeTimeout());
                }
            });
            lastSuccessfulHeartbeat = clock.instant();
        }catch (RuntimeException ex){
            log.warn("Cluster heartbeat of node '{}' failed: {}",properties.nodeId(),ex.getMessage());
        }
    }
    @PreDestroy
    public void stop(){
        if (!properties.isEnabled()){
            return;
        }
        ScheduledFuture<?>task = heartbeatTask;
        if (task != null){
            task.cancel(false);
        }
        try {
            tx.executeWithoutResult(s-> repository.findById(properties.nodeId())
                    .ifPresent(node -> node.leave(clock.instant())));
            log.info("Cluster node '{}' left the cluster",properties.nodeId());
        }catch (RuntimeException ex){
            log.warn("Could not mark node '{}' as LEFT: {}",properties.nodeId(),ex.getMessage());
        }
    }
    public List<ServerClusterNode>activeNodes(){
        return tx.execute(s->repository.findByStatusOrderByNodeId(NodeStatus.UP));
    }
    public String nodeId(){
        return properties.nodeId();
    }
    public boolean isEnabled(){return properties.isEnabled();}

    public boolean isHeartbeatStale(){
        Instant last = lastSuccessfulHeartbeat;
        return last == null || last.isBefore(clock.instant().minus(properties.nodeTimeout()));
    }
    public Instant lastSuccessfulHeartbeat(){
        return lastSuccessfulHeartbeat;
    }

    private static String hostname(){
        try {
            return InetAddress.getLocalHost().getHostName();
        }catch (Exception ex){
            return "unknown";
        }
    }
}
