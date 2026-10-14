package com.coelhotechne.detection_system.detection.domain;

import org.junit.jupiter.api.Test;

import  static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BoundingBoxTest {
    @Test
    void yoloOrderRoundTrips(){
        int[] xyxy= {10,20,100,200};
        BoundingBox boundingBox = BoundingBox.of(xyxy);
        assertThat(boundingBox.x1()).isEqualTo(10);
        assertThat(boundingBox.y1()).isEqualTo(20);
        assertThat(boundingBox.x2()).isEqualTo(100);
        assertThat(boundingBox.y2()).isEqualTo(200);
    }
    @Test
    void nullMeansNoBox(){assertThat(BoundingBox.of(null)).isNull();}
    @Test
    void rejectsInvertedCorners(){
        assertThatThrownBy(()-> BoundingBox.of(new int[]{100,20,10,200}))
                .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(()-> BoundingBox.of(new int[]{10,200,100,20}))
                .isInstanceOf(IllegalArgumentException.class);
    }
    @Test
    void rejectsWrongSizeAndNegative(){
        assertThatThrownBy(()-> BoundingBox.of(new int[]{1,2,3}))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()-> BoundingBox.of(new int[]{-1,2,30,40}))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
