package com.coelhotechne.detection_system.detection.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record BoundingBox(
        @Column(name = "bbox_x1", updatable = false) int x1,
        @Column(name = "bbox_y1", updatable = false) int y1,
        @Column(name = "bbox_x2", updatable = false) int x2,
        @Column(name = "bbox_y2", updatable = false) int y2
) {
    public BoundingBox {
        if (x1 < 0 || y1 < 0) {
            throw new IllegalArgumentException("bbox coordinates must not be negative");
        }
        if (x1 >= x2 || y1 >= y2) {
            throw new IllegalArgumentException("bbox must be [x1, y1, x2, y2] with x1 < x2 and y1 < y2");
        }
    }

    /** {@code null} quando a origem não mandou caixa. */
    public static BoundingBox of(int[] xyxy) {
        if (xyxy == null) {
            return null;
        }
        if (xyxy.length != 4) {
            throw new IllegalArgumentException("bbox must have exactly 4 values: [x1, y1, x2, y2]");
        }
        return new BoundingBox(xyxy[0], xyxy[1], xyxy[2], xyxy[3]);
    }

    public int[] toArray() {
        return new int[]{x1, y1, x2, y2};
    }

    public int centerX() {
        return (x1 + x2) / 2;
    }

    public int centerY() {
        return (y1 + y2) / 2;
    }
}