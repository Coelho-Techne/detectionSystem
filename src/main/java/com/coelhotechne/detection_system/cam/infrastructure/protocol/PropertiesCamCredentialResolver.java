package com.coelhotechne.detection_system.cam.infrastructure.protocol;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
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
    public RtspCredentials resolve(String credentialRef,String targetHost) {

        if (credentialRef==null||credentialRef.isBlank()){
            return null;
        }
        Entry entry = properties.credentials.get(credentialRef);
        if (entry==null||entry.username() == null||entry.password()==null){
            log.warn("No credentials configured for credentialRef={} (cam.credentials.{}.username/password)",
                    credentialRef, credentialRef);
            return null;
        }if (!entry.allowHost(targetHost)){
            log.warn("credentialRef={} is not allowed for host {} (cam.credentials.{}.hosts={})",
                    credentialRef,targetHost,credentialRef,entry.hosts());
            return null;
        }
        return new RtspCredentials(entry.username,entry.password.toCharArray(),entry.allowBasic());
    }

    @ConfigurationProperties(prefix = "cam")
    public record CamCredentialProperties(Map<String,Entry>credentials){
        public CamCredentialProperties {
            credentials = credentials==null ?Map.of():Map.copyOf(credentials);
        }
    }

    public record Entry(String username, String password, List<String> hosts,boolean allowBasic){
        public Entry{
            hosts = hosts == null ? List.of():hosts.stream().filter(h-> h!=null && !h.isBlank())
                    .map(h-> h.trim().toLowerCase(Locale.ROOT))
                    .toList();
        }
        boolean allowHost(String host){
            return host!=null&& hosts.contains(host.trim().toLowerCase(Locale.ROOT));
        }
        @Override
        public String toString(){
            return "Entry[username="+username+"password=***********, hosts="+hosts+", allowBasic="+allowBasic+"]";
        }
    }
}
