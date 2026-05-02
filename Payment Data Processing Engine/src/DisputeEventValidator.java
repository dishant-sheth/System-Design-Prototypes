package src;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import src.interfaces.IEventValidator;
import src.models.DisputeEvent;
import src.models.Event;
import src.models.EventValidatorResult;

public class DisputeEventValidator implements IEventValidator {

    private static Pattern reasonCodeMatcher = Pattern.compile("^RC[0-9]{3}$");

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

    private boolean validateReasonCode(final String reasonCode, List<String> errorMessages){
        final boolean isValid = reasonCodeMatcher.matcher(reasonCode).matches();
        if(!isValid) errorMessages.add("Reason code " + reasonCode + " is invalid");
        return isValid;
    }

    @Override
    public EventValidatorResult validate(Event event){
        if(!(event instanceof DisputeEvent)){
            return new EventValidatorResult(false, new ArrayList<>());
        }

        DisputeEvent disputeEvent = (DisputeEvent) event;
        List<String> validationErrors = new ArrayList<>();
        final boolean isValid = validateTxnId(disputeEvent.txn_id, validationErrors)
            && validateMerchantId(disputeEvent.merchant_id, validationErrors)
            && validateTimestamp(disputeEvent.timestamp, validationErrors)
            && validateReasonCode(disputeEvent.reason_code, validationErrors);

        return new EventValidatorResult(isValid, validationErrors);
    }
}