package com.coelhotechne.detection_system.globalClass.security;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class AccessKeyHasher {
    private static final String ALGORITHM = "SHA-256";

    public String hash(String rawAccessKey){
        if (rawAccessKey == null){
            throw new IllegalArgumentException("access key is required");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hashBytes = digest.digest(rawAccessKey.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        }catch (NoSuchAlgorithmException e){
            throw new IllegalArgumentException("Hash algorithm unavailable "+ALGORITHM,e);
        }
    }

    public boolean matches(String rawAccessKey, String storedHash){
        if (rawAccessKey == null|| rawAccessKey.isBlank()||storedHash==null){
            return false;
        }
        byte[] computed = hash(rawAccessKey).getBytes(StandardCharsets.UTF_8);
        byte[] stored = storedHash.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(computed,stored);
    }
}
