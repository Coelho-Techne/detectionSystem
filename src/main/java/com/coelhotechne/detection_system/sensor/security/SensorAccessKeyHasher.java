package com.coelhotechne.detection_system.sensor.security;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class SensorAccessKeyHasher {
    private static final String ALGORITHM = "SHA-256";

    public String hash(String rawAccessKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hashBytes = digest.digest(rawAccessKey.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo de hash indisponível: " + ALGORITHM, e);
        }
    }

    public boolean matches(String rawAccessKey, String storedHash) {
        if (rawAccessKey == null || storedHash == null) {
            return false;
        }
        byte[] computed = hash(rawAccessKey).getBytes(StandardCharsets.UTF_8);
        byte[] stored = storedHash.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(computed, stored); // constant-time, não String.equals
    }
}
