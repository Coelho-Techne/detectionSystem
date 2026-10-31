package com.coelhotechne.detection_system.cam.infrastructure.protocol;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Log4j2
@Component
@EnableConfigurationProperties(PropertiesCamCredentialResolver.CamCredentialProperties.class)
public class PropertiesCamCredentialResolver implements CamCredentialResolver{

    private final CamCredentialProperties properties;

    public PropertiesCamCredentialResolver(CamCredentialProperties properties){
        this.properties = properties;
    }

    @Override
    public RtspCredentials resolve(String credentialRef) {

        if (credentialRef==null||credentialRef.isBlank()){
            return null;
        }
        Entry entry = properties.credentials.get(credentialRef);
        if (entry==null||entry.username() == null||entry.password()==null){
            log.warn("No credentials configured for credentialRef={} (cam.credentials.{}.username/password)",
                    credentialRef, credentialRef);
            return null;
        }
        return new RtspCredentials(entry.username,entry.password.toCharArray());
    }

    @ConfigurationProperties(prefix = "cam")
    public record CamCredentialProperties(Map<String,Entry>credentials){
        public CamCredentialProperties {
            credentials = credentials==null ?Map.of():Map.copyOf(credentials);
        }
    }

    public record Entry(String username, String password){
        @Override
        public String toString(){
            return "Entry[username="+username+"password=***********]";
        }
    }
}
