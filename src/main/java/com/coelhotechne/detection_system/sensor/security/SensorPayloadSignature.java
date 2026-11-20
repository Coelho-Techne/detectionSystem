package com.coelhotechne.detection_system.sensor.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;

public final class SensorPayloadSignature {
    private static final int HEX_LENGTH = 64;
    private static final String ALGORITHM  = "HmacSHA256";

    private SensorPayloadSignature(){}

    public record Parsed(String signatureHex,String body){
        public boolean isSigned(){return signatureHex!=null;}

    }
    public static Parsed parse(String payload){
        if ((payload != null) && (payload.length() > HEX_LENGTH) && (payload.charAt(HEX_LENGTH) == '.')
                && payload.substring(0, HEX_LENGTH).chars().allMatch(c -> Character.digit(c, 16) >= 0)){
            return new Parsed(payload.substring(0,HEX_LENGTH),payload.substring(HEX_LENGTH+1));
        }
        return new Parsed(null, payload);
    }

    public static boolean verify(String storedKeyHash, String topic, Parsed parsed){
        if(storedKeyHash == null||!parsed.isSigned()){
            return false;
        }
        byte[] expected = sign(storedKeyHash,topic,parsed.body());
        byte[] presented = HexFormat.of().parseHex(parsed.signatureHex().toLowerCase());
        return MessageDigest.isEqual(expected,presented);
    }

    public static String signHex(String storedKeyHash, String topic, String body){
        return HexFormat.of().formatHex(sign(storedKeyHash,topic,body));
    }

    private static byte[] sign(String storedKeyHash, String topic, String body){
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(storedKeyHash.getBytes(StandardCharsets.UTF_8),ALGORITHM));
            mac.update(topic.getBytes(StandardCharsets.UTF_8));
            mac.update((byte)'\n');
            return mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
        }catch (GeneralSecurityException ex){
            throw new IllegalStateException("HMAC-SHA256 unavailable: ",ex);
        }
    }
}
