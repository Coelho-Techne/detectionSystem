package com.coelhotechne.detection_system.installation.application;

import com.coelhotechne.detection_system.installation.api.dto.InstallationRequest;
import com.coelhotechne.detection_system.installation.domain.Installation;
import com.coelhotechne.detection_system.location.application.LocationService;
import com.coelhotechne.detection_system.location.domain.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InstallationResolver {
    private final LocationService locationService;

    public Installation resolve(InstallationRequest request){
        if (request==null){
            return null;
        }
        Location location = request.locationId() == null ? null : locationService.requireLocation(request.locationId());
        return request.toEntity(location);
    }
}
