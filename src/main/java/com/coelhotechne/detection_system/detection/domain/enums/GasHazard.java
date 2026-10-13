package com.coelhotechne.detection_system.detection.domain.enums;

public enum GasHazard {
    /** Risco de ignição. Resposta: fechar o registro, não acionar nada elétrico, ventilar. */
    COMBUSTIBLE,
    /** Risco à saúde por inalação. Resposta: sair do ambiente e ventilar. */
    TOXIC,
    /** Desloca o oxigênio. Resposta: sair do ambiente; não entrar sem ar respirável. */
    ASPHYXIANT,
    /** Código fora do catálogo. Tratado como vazamento: na dúvida, alarme, não silêncio. */
    UNKNOWN
}
