package src;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import src.interfaces.IFraudDetector;
import src.interfaces.IFraudRule;
import src.models.ChargeEvent;
import src.models.FraudRuleResult;


public class FraudDetector implements IFraudDetector {

    public final List<IFraudRule> fraudRules;

    public FraudDetector(final List<IFraudRule> fraudRules){
        this.fraudRules = fraudRules;
    }
    
    @Override
    public Set<String> detectSuspiciousMerchants(List<ChargeEvent> charges){
        final Set<String> resultSet = new HashSet<>();
        for(ChargeEvent charge: charges){
            for(IFraudRule rule: fraudRules){
                final FraudRuleResult ruleResult = rule.isSuspicious(charge);
                if(ruleResult.ruleViolated()){
                    resultSet.add(ruleResult.violationMessage());
                }
            }
        }

        return resultSet;
    }
}