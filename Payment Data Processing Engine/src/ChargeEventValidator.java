package src;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import src.interfaces.IEventValidator;
import src.models.ChargeEvent;
import src.models.Event;
import src.models.EventValidatorResult;

public class ChargeEventValidator implements IEventValidator {

    final static Set<String> whitelistedCurrencies = Set.of("USD", "GBP", "INR", "SGD", "EUR");

    private boolean validateTxnId(final String txnId, List<String> errorMessages){
        final boolean isValid = txnIdPattern.matcher(txnId).matches();
        if(!isValid) errorMessages.add("Txn ID " + txnId + " is invalid");
        return isValid;
    }

    private boolean validateMerchantId(final String merchantId, List<String> errorMessages){
        final boolean isValid = merchantIdPattern.matcher(merchantId).matches();
        if(!isValid) errorMessages.add("Merchant ID " + merchantId + " is invalid");
        return isValid;
    }

    private boolean validateTimestamp(final String timestamp, List<String> errorMessages){
        final boolean isValid = timestampPattern.matcher(timestamp).matches();
        if(!isValid) errorMessages.add("Timestamp " + timestamp + " is invalid");
        return isValid;
    }

    private boolean validateCurrency(final String currency, List<String> errorMessages){
        final boolean isValid = whitelistedCurrencies.contains(currency);
        if(!isValid) errorMessages.add("Currency " + currency + " is invalid");
        return isValid;
    }

    private boolean validateAmount(final BigDecimal amount, List<String> errorMessages){
        // Check if amount is positive
        if(amount.signum() <= 0){
            errorMessages.add(amount + " is invalid");
            return false;
        }
        if(amount.scale() != 2){
            errorMessages.add(amount + " is invalid");
            return false;
        }
        return true;
    }

    @Override
    public EventValidatorResult validate(Event event){
        if(!(event instanceof ChargeEvent)){
            return new EventValidatorResult(false, new ArrayList<>());
        }

        ChargeEvent chargeEvent = (ChargeEvent) event;
        List<String> validationErrors = new ArrayList<>();
        final boolean isValid = validateTxnId(chargeEvent.txn_id, validationErrors)
            && validateMerchantId(chargeEvent.merchant_id, validationErrors)
            && validateTimestamp(chargeEvent.timestamp, validationErrors)
            && validateCurrency(chargeEvent.currency, validationErrors)
            && validateAmount(chargeEvent.amount, validationErrors);

        return new EventValidatorResult(isValid, validationErrors);
    }
}