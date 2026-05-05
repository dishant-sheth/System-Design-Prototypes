package src.interfaces;

import src.models.ChargeEvent;
import src.models.FraudRuleResult;

public interface IFraudRule {
    public FraudRuleResult isSuspicious(ChargeEvent incomingChargeEvent);
}