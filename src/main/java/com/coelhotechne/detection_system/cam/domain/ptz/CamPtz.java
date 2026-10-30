package com.coelhotechne.detection_system.cam.domain.ptz;

import com.coelhotechne.detection_system.cam.domain.base.BaseCam;
import com.coelhotechne.detection_system.cam.domain.ptz.enums.PtzAction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

@Setter
@Getter
@Entity
@NoArgsConstructor
@Table(name = "ptz_cam")
@EqualsAndHashCode(callSuper = true)
@EntityListeners(AuditingEntityListener.class)
public class CamPtz extends BaseCam {
    @Column(name = "pan_left")
    private Float panLeft;
    @Column(name = "pan_right")
    private Float panRight;
    @Column(name = "tilt_down")
    private Float tiltDown;
    @Column(name = "tilt_up")
    private Float tiltUp;
    @Column(name = "zoom_out")
    private Float zoomOut;
    @Column(name = "zoom_in")
    private Float zoomIn;

    @Column(name = "presets", columnDefinition = "TEXT")
    private String presets;
    @Setter(AccessLevel.NONE)
    @Column(name = "current_preset")
    private Integer currentPreset;
    @Setter(AccessLevel.NONE)
    @Column(name = "current_pan")
    private Float currentPan;
    @Setter(AccessLevel.NONE)
    @Column(name = "current_tilt")
    private Float currentTilt;
    @Setter(AccessLevel.NONE)
    @Column(name = "current_zoom")
    private Float currentZoom;
    @Setter(AccessLevel.NONE)
    @Column(name = "last_ptz_command", length = 120)
    private String lastPtzCommand;
    @Setter(AccessLevel.NONE)
    @Column(name = "last_ptz_command_at")
    private Instant lastPtzCommandAt;
    @Setter(AccessLevel.NONE)
    @Column(name = "is_moving")
    private Boolean moving;

    public void applyCommand(PtzAction action,Float value, Instant at){
        Objects.requireNonNull(action,"Action is required");
        switch (action){
            case PAN -> this.currentPan = requireWhithin("pan",value,panLeft,panRight);
            case TILT -> this.currentTilt = requireWhithin("tilt",value,tiltDown,tiltUp);
            case ZOOM -> this.currentZoom =requireWhithin("zoom",value,zoomOut,zoomIn);
            case GOTO_PRESET -> {
                if (value==null||value<0 || value%1!=0){
                    throw new IllegalArgumentException("preset must be a non-negative integer");
                }
                this.currentPreset = value.intValue();
            }
            case STOP -> { }
        }
        this.moving=action!=PtzAction.STOP;
        this.lastPtzCommand = value==null ? action.name() : action.name() + ":" +value;
        this.lastPtzCommandAt = at;
    }

    private static Float requireWhithin(String axis, Float value,Float min,Float max){
        if (value == null){
            throw new IllegalArgumentException(axis+" value is required");
        }
        if ((min!=null && value<=min) || (max!=null && value> max)){
            throw new IllegalArgumentException("%s %s outside the configured limits [%s %s]".formatted(axis,value,min,max));
        }
        return value;
    }
}
