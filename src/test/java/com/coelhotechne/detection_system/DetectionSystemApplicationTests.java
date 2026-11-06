package com.coelhotechne.detection_system;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DetectionSystemApplicationTests {

    @Autowired
    private MqttClient mqttClient;

	@Test
	void contextLoads() {
        assertThat(mqttClient).isNotNull();
        assertThat(mqttClient.isConnected()).isFalse();
	}

}
