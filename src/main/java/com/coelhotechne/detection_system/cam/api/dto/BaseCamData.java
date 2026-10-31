package com.coelhotechne.detection_system.cam.api.dto;

import com.coelhotechne.detection_system.batterysupply.domain.PowerSupply;
import com.coelhotechne.detection_system.cam.domain.base.BaseCam;
import com.coelhotechne.detection_system.installation.api.dto.InstallationRequest;
import com.coelhotechne.detection_system.installation.domain.Installation;
import com.coelhotechne.detection_system.zone.domain.Zone;

import java.util.UUID;

public interface BaseCamData {
    String camModel();
    String firmwareVersion();
    String serialNumber();
    String macAddress();
    String ipAddress();
    String rtsp();
    Float fps();
    Integer bitrate();
    String compression();
    String frameUrl();
    UUID zoneId();
    PowerSupply powerSupply();
    InstallationRequest installation();

    static void apply(BaseCam baseCam, BaseCamData data, Zone zone, Installation installation){
        baseCam.setCamModel(data.camModel());
        baseCam.setFirmwareVersion(data.firmwareVersion());
        baseCam.setSerialNumber(data.serialNumber());
        baseCam.setMacAddress(data.macAddress());
        baseCam.setIpAddress(data.ipAddress());
        baseCam.setRtsp(data.rtsp());
        baseCam.setFps(data.fps());
        baseCam.setBitrate(data.bitrate());
        baseCam.setCompression(data.compression());
        baseCam.setFrameUrl(data.frameUrl());
        baseCam.setZone(zone);
        baseCam.setPowerSupply(data.powerSupply());
        baseCam.setInstallation(installation);
    }

}
