package com.coelhotechne.detection_system.cam.api.ptz;

import com.coelhotechne.detection_system.cam.api.dto.BaseCamData;
import com.coelhotechne.detection_system.cam.api.dto.fixed.CamConnectionSummary;
import com.coelhotechne.detection_system.cam.api.dto.fixed.CamHomologationSummary;
import com.coelhotechne.detection_system.cam.domain.ptz.CamPtz;
import com.coelhotechne.detection_system.installation.api.dto.InstallationResponse;
import com.coelhotechne.detection_system.installation.domain.Installation;
import com.coelhotechne.detection_system.zone.domain.Zone;
import org.springframework.stereotype.Component;


@Component
public class CamPtzMapper{
    public void apply(CamPtz camPtz, CamPtzRequest request, Zone zone, Installation installation){
        BaseCamData.apply(camPtz,request,zone,installation);
        if (request.panLeft()!=null&&request.panRight()!=null&& request.panLeft() > request.panRight()){
            throw new IllegalArgumentException("pan_left must be <= pan_right");
        }
        if (request.tiltDown()!=null&& request.tiltUp()!=null && request.tiltDown() > request.tiltUp()){
            throw new IllegalArgumentException("tilt_down must be <= tilt_up");
        }
        if (request.zoomOut() != null && request.zoomIn() != null && request.zoomOut() > request.zoomIn()){
            throw new IllegalArgumentException("zoom_out must be <= zoom_in");
        }

        camPtz.setPanLeft(request.panLeft());
        camPtz.setPanRight(request.panRight());
        camPtz.setTiltDown(request.tiltDown());
        camPtz.setTiltUp(request.tiltUp());
        camPtz.setZoomOut(request.zoomOut());
        camPtz.setZoomIn(request.zoomIn());
        camPtz.setPresets(request.presets());
    }

    public CamPtzResponse toResponse(CamPtz camPtz){
        return new CamPtzResponse(
                camPtz.getUuid(),
                camPtz.getVersion(),
                camPtz.getCamModel(),
                camPtz.getFirmwareVersion(),
                camPtz.getSerialNumber(),
                camPtz.getMacAddress(),
                camPtz.getIpAddress(),
                camPtz.getRtsp(),
                camPtz.getCamStatus(),
                camPtz.getFps(),
                camPtz.getBitrate(),
                camPtz.getCompression(),
                camPtz.getFrameUrl(),
                camPtz.getPowerSupply(),
                InstallationResponse.from(camPtz.getInstallation()),
                camPtz.getZone()==null ?null: camPtz.getZone().getUuid(),
                CamConnectionSummary.from(camPtz.getCamConnection()),
                camPtz.getAccessKey() !=null,
                camPtz.getLastCommunication(),
                CamHomologationSummary.from(camPtz.getHomologation()),
                camPtz.getPanLeft(),
                camPtz.getPanRight(),
                camPtz.getTiltDown(),
                camPtz.getTiltUp(),
                camPtz.getZoomOut(),
                camPtz.getZoomIn(),
                camPtz.getPresets(),
                camPtz.getCurrentPreset(),
                camPtz.getCurrentPan(),
                camPtz.getCurrentTilt(),
                camPtz.getCurrentZoom(),
                camPtz.getLastPtzCommand(),
                camPtz.getLastPtzCommandAt(),
                camPtz.getMoving()
        );
    }
}
