package com.coelhotechne.detection_system.alert.application;

import com.coelhotechne.detection_system.alert.api.dto.AlertResponse;
import com.coelhotechne.detection_system.alert.api.dto.AlertSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AlertService {
    Page<AlertResponse>search(AlertFilter filter, Pageable pageable);
    AlertResponse get(UUID id);
    AlertSummaryResponse summary();
    AlertResponse acknowledge(UUID id,String user,Long expectedVersion);
    AlertResponse resolve(UUID id, String user,String note, Long expectedVersion);
}
