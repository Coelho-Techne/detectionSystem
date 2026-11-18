package com.coelhotechne.detection_system.cam.infrastructure.protocol;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.InetAddress;
import java.util.List;

@ConfigurationProperties(prefix = "cam.probe")
public record CamProbeProperties (List<String> allowedNetworks,Boolean allowLoopback){

    public CamProbeProperties{
        allowedNetworks = allowedNetworks == null ? List.of():allowedNetworks.stream().filter(n -> n !=null && !n.isBlank())
                .map(String::trim).toList();
        allowLoopback = allowLoopback!=null && allowLoopback;
        allowedNetworks.forEach(CamProbeProperties::parseCidr);
    }

    public String rejectionReason(InetAddress address){
        if (!allowLoopback && (address.isLoopbackAddress() || address.isAnyLocalAddress())
        || address.isLinkLocalAddress() || address.isMulticastAddress()){
            return "loopback, link-local, multicast and wildcard addresses are not allowed";
        }
        if (allowedNetworks.isEmpty()){
            return null;
        }
        for (String network : allowedNetworks){
            if (contains(network,address)){
                return null;
            }
        }
        return "address outside cam.probe.allowed-networks";
    }

    static boolean contains(String cidr, InetAddress address){
        Cidr parsed = parseCidr(cidr);
        byte[] target = address.getAddress();
        if (target.length != parsed.network().length){
            return false;
        }
        int fullBytes = parsed.prefix()/8;
        int remainingBits = parsed.prefix % 8;
        for (int i = 0; i<fullBytes;i++){
            if (target[i]!=parsed.network()[i]){
                return false;
            }
        }
        if (remainingBits == 0){
            return true;
        }
        int mask = (0xFF << (8 - remainingBits))& 0xFF;
        return (target[fullBytes]&mask) == (parsed.network()[fullBytes]&mask);
    }
    private record Cidr(byte[] network,int prefix){}

    private static Cidr parseCidr(String cidr){
        String[] parts = cidr.split("/",2);
        try {
            if (!parts[0].matches("[0-9a-fA-F:.]+")){
                throw new IllegalArgumentException("not an Ip literal");
            }
            byte[] network = InetAddress.getByName(parts[0]).getAddress();
            int max = network.length*8;
            int prefix = parts.length == 2 ? Integer.parseInt(parts[1]) : max;
            if (prefix<0||prefix>max){
                throw new IllegalArgumentException("prefix out of range");
            }
            return new Cidr(network,prefix);
        }catch (Exception ex){
            throw new IllegalArgumentException("Invalid cam.probe.allowed-networks entry: "+cidr,ex);
        }
    }
}
