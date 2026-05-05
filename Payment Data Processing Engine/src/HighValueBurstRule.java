package src;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import src.interfaces.IFraudRule;
import src.models.ChargeEvent;
import src.models.FraudRuleResult;

public class HighValueBurstRule implements IFraudRule {

    private static final String RULE_NAME = "HIGH_VALUE_BURST_FRAUD_RULE";
    // Rule to manage it's own needed attributes.
    private final Map<String, Deque<ChargeEvent>> merchantChargeMap;
    private final Map<String, BigDecimal> merchantAmountMap;
    private final int windowSizeInSeconds;
    private final BigDecimal maxAmountAllowed;

    public HighValueBurstRule(final int windowSizeInSeconds, final BigDecimal maxAmountAllowed){
        this.merchantChargeMap = new HashMap<>();
        this.merchantAmountMap = new HashMap<>();
        this.windowSizeInSeconds = windowSizeInSeconds;
        this.maxAmountAllowed = maxAmountAllowed;
    }

    @Override
    public FraudRuleResult isSuspicious(ChargeEvent incomingChargeEvent) {
        final String merchantId = incomingChargeEvent.merchant_id;
        if(!merchantChargeMap.containsKey(merchantId)){
            merchantChargeMap.put(merchantId, new ArrayDeque<>());
        }

        final Deque<ChargeEvent> merchantWindow = merchantChargeMap.get(merchantId);
        final LocalDateTime currTime = LocalDateTime.parse(incomingChargeEvent.timestamp);
        // Empty the queue first.
        while(!merchantWindow.isEmpty()){
            final LocalDateTime windowDateTime = LocalDateTime.parse(merchantWindow.getFirst().timestamp);

            if(Duration.between(windowDateTime, currTime).getSeconds() > windowSizeInSeconds){
                ChargeEvent removedEvent = merchantWindow.removeFirst();
                BigDecimal currMerchantAmount = merchantAmountMap.get(removedEvent.merchant_id);
                currMerchantAmount = currMerchantAmount.subtract(removedEvent.amount);
                merchantAmountMap.put(removedEvent.merchant_id, currMerchantAmount);
            } else {
                break;
            }
        }

        merchantWindow.add(incomingChargeEvent);
        BigDecimal currMerchantAmount = merchantAmountMap.getOrDefault(incomingChargeEvent.merchant_id, BigDecimal.ZERO);
        currMerchantAmount = currMerchantAmount.add(incomingChargeEvent.amount);
        merchantAmountMap.put(incomingChargeEvent.merchant_id, currMerchantAmount);

        if(merchantAmountMap.get(incomingChargeEvent.merchant_id).subtract(maxAmountAllowed).compareTo(BigDecimal.ZERO) > 0){
            final String errorMessage = "-> " + incomingChargeEvent + " flagged by " + RULE_NAME;
            FraudRuleResult result = new FraudRuleResult(true, errorMessage);
            return result;
        }
        return new FraudRuleResult(false, new String());
    }
}