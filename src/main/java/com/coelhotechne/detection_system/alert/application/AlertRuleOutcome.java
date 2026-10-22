package com.coelhotechne.detection_system.alert.application;

import com.coelhotechne.detection_system.alert.domain.AlertDecision;

import java.util.List;

public record AlertRuleOutcome(
        List<String>clearPrefixes, AlertDecision raise
        ){
    public static final AlertRuleOutcome NONE = new AlertRuleOutcome(List.of(),null);
    public AlertRuleOutcome{
        clearPrefixes = List.copyOf(clearPrefixes);
    }

    public static AlertRuleOutcome raise(AlertDecision decision){
        return new AlertRuleOutcome(List.of(),decision);
    }
    public static AlertRuleOutcome clear(String... prefixes){
        return new AlertRuleOutcome(List.of(prefixes),null);
    }
    public AlertRuleOutcome andRaise(AlertDecision decision){
        return new AlertRuleOutcome(clearPrefixes,decision);
    }
}
