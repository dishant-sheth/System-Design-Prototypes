package src;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import src.interfaces.IFraudRule;
import src.models.ChargeEvent;
import src.models.FraudRuleResult;

public class HighVelocityFraudRule implements IFraudRule {

    private static final String RULE_NAME = "HIGH_VELOCITY_FRAUD_RULE";

    private final Map<String, Deque<ChargeEvent>> merchantChargeMap;
    private final int windowSizeInSeconds;
    private final int maxAllowedCharges;

    public HighVelocityFraudRule(final int windowSizeInSeconds, final int maxAllowedCharges){
        this.merchantChargeMap = new HashMap<>();
        this.windowSizeInSeconds = windowSizeInSeconds;
        this.maxAllowedCharges = maxAllowedCharges;
    }

    @Override
    public FraudRuleResult isSuspicious(ChargeEvent incomingChargeEvent) {
        final String merchantId = incomingChargeEvent.merchant_id;
        if(!merchantChargeMap.containsKey(merchantId)){
            merchantChargeMap.put(merchantId, new ArrayDeque<>());
        }

        final Deque<ChargeEvent> merchantWindow = merchantChargeMap.get(merchantId);

        LocalDateTime currTime = LocalDateTime.now();
        while(!merchantWindow.isEmpty()){
            LocalDateTime windowStartTime = LocalDateTime.parse(merchantWindow.getFirst().timestamp);
            
            if(Duration.between(windowStartTime, currTime).getSeconds() > windowSizeInSeconds){
                merchantWindow.removeFirst();
            } else {
                break;
            }
        }

        merchantWindow.add(incomingChargeEvent);
        if(merchantWindow.size() > maxAllowedCharges){
            final String errorMessage = "-> " + incomingChargeEvent + " flagged by " + RULE_NAME;
            return new FraudRuleResult(true, errorMessage);
        }
        return new FraudRuleResult(false, "");
    }
}