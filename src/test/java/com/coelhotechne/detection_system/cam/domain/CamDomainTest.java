package com.coelhotechne.detection_system.cam.domain;


import com.coelhotechne.detection_system.cam.domain.connectioncam.CamConnectionParams;
import com.coelhotechne.detection_system.cam.domain.connectioncam.enums.CamProtocol;
import com.coelhotechne.detection_system.cam.domain.fixed.CamFixed;
import com.coelhotechne.detection_system.cam.domain.homologation.CamConnectionTestOutcome;
import com.coelhotechne.detection_system.cam.domain.homologation.enums.CamHomologationStatus;
import com.coelhotechne.detection_system.cam.domain.ptz.CamPtz;
import com.coelhotechne.detection_system.cam.domain.ptz.enums.PtzAction;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CamDomainTest {

    private static final Instant T0 = Instant.parse("2026-11-05T16:00:00Z");

    @Test
    void approvedCameraCannotBeRetestedWithoutReopen(){
        CamFixed camFixed = new CamFixed();
        camFixed.getHomologation().applyTestResult(CamConnectionTestOutcome.success("H264"),T0);
        camFixed.getHomologation().approve("admin",T0);
        assertThatThrownBy(()-> camFixed.getHomologation()
                .applyTestResult(CamConnectionTestOutcome.success("H264"), T0))
                .isInstanceOf(IllegalStateException.class);
        camFixed.getHomologation().reopen();
        assertThat(camFixed.getHomologation().getStatus()).isEqualTo(CamHomologationStatus.PENDING_TEST);
    }

    @Test
    void chargingConnectionResetsHomologation(){
        CamFixed cam = new CamFixed();
        cam.getHomologation().applyTestResult(CamConnectionTestOutcome.success("H264"),T0);
        cam.getHomologation().approve("admin",T0);
        cam.configureCamConnection(CamProtocol.RTSP,
                new CamConnectionParams("rtsp://10.0.0.9/s1",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null));

        assertThat(cam.getHomologation().getStatus()).isEqualTo(CamHomologationStatus.PENDING_TEST);
        assertThat(cam.getCamConnection().getNetworkLink()).isNull();
    }
    @Test
    void rtspRequiresStreamUri(){
        CamFixed camFixed= new CamFixed();
        assertThatThrownBy(()-> camFixed.configureCamConnection(
                CamProtocol.RTSP,
                new CamConnectionParams(" ",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null)))
                .isInstanceOf(IllegalArgumentException.class);
    }
    @Test
    void ptzCommandRespectsLimits(){
        CamPtz camPtz = new CamPtz();
        camPtz.setPanLeft(-90f);
        camPtz.setPanRight(90f);
        camPtz.applyCommand(PtzAction.PAN,45f,T0);
        assertThat(camPtz.getCurrentPan()).isEqualTo(45f);
        assertThat(camPtz.getMoving()).isTrue();
        assertThatThrownBy(()-> camPtz.applyCommand(PtzAction.PAN,120f,T0))
                .isInstanceOf(IllegalArgumentException.class);
        camPtz.applyCommand(PtzAction.STOP,null,T0);
        assertThat(camPtz.getMoving()).isFalse();
        assertThat(camPtz.getMoving()).isFalse();
        assertThat(camPtz.getLastPtzCommand()).isEqualTo("STOP");
    }
}
