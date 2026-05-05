package src.models;

public record FraudRuleResult(
    boolean ruleViolated,
    String violationMessage
){}