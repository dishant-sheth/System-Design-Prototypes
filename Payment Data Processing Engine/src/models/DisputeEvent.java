package src.models;

import java.util.regex.Pattern;

public class DisputeEvent extends Event {  
    String reason_code;

    public DisputeEvent(Builder builder){
        this.txn_id = builder.txn_id;
        this.merchant_id = builder.merchant_id;
        this.timestamp = builder.timestamp;
        this.reason_code = builder.reason_code;
    }

    public boolean validateReasonCode(){
        final boolean isValid = Pattern.matches("^RC[0-9]{3}$", reason_code);
        if(!isValid) System.out.println("Invalid " + reason_code);
        return isValid;
    }

    public boolean isValid(){
        boolean isValid = true;
        isValid &= validateTxnId();
        isValid &= validateMerchantId();
        isValid &= validateTimestamp();
        isValid &= validateReasonCode();
        return isValid;
    }

    public static class Builder {

        String reason_code;
        String txn_id;
        String merchant_id;
        String timestamp;

        public static Builder newInstance(){
            return new Builder();
        }

        public Builder txnId(final String txnId){
            this.txn_id = txnId;
            return this;
        }

        public Builder merchantId(final String merchantId){
            this.merchant_id = merchantId;
            return this;
        }

        public Builder timestamp(final String timestamp){
            this.timestamp = timestamp;
            return this;
        }

        public Builder reasonCode(final String reasonCode){
            this.reason_code = reasonCode;
            return this;
        }

        public DisputeEvent build(){
            DisputeEvent event = new DisputeEvent(this);
            if(event.isValid()) return event;
            return null;
        }

    }

}