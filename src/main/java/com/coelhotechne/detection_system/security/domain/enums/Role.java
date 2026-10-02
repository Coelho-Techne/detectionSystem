package com.coelhotechne.detection_system.security.domain.enums;

import lombok.Getter;

@Getter
public enum Role {
    ADMIN("Admin"),
    TECHNICIAN("Technician"),//TECNICO RESPONSAVEL A DISTANCIA OU PRESENCIAL
    AI_USER("AI User"),//IA apenas de leitura, ainda não autorizada a realizar alteracoes.
    HOME_USER("Home User"),
    DEVICE_USER("Device User"),
    DEVELOPER("Developer"); //desenvolvedor nao presente no local

    private static final String AUTHORITY_PREFIX = "ROLE_";

    private final String label;

    Role(String label) {
        this.label = label;
    }

    /** Authority do Spring Security, ex.: ROLE_HOME_USER */
    public String authority() {
        return AUTHORITY_PREFIX + name();
    }
}
