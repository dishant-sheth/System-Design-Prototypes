package src.models;

public class DisputeEvent extends Event {  
    public String reason_code;

    public DisputeEvent(Builder builder){
        this.txn_id = builder.txn_id;
        this.merchant_id = builder.merchant_id;
        this.timestamp = builder.timestamp;
        this.reason_code = builder.reason_code;
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
            return new DisputeEvent(this);
        }

    }

}