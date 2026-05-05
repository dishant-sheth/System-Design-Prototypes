package src.models;

import java.math.BigDecimal;

public class ChargeEvent extends Event {
    public final String currency;
    public final BigDecimal amount;

    public ChargeEvent(Builder builder){
        this.txn_id = builder.txn_id;
        this.merchant_id = builder.merchant_id;
        this.timestamp = builder.timestamp;
        this.currency = builder.currency;
        this.amount = builder.amount;
    }

    @Override
    public String toString(){
        return this.timestamp + ", " + this.txn_id + ", " + this.merchant_id + ", " + this.amount.toPlainString() + this.currency; 
    }

    public static class Builder {
        
        String txn_id;
        String merchant_id;
        String timestamp;
        String currency;
        BigDecimal amount;

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

        public Builder currency(String currency){
            this.currency = currency;
            return this;
        }

        public Builder amount(BigDecimal amount){
            this.amount = amount;
            return this;
        }

        public ChargeEvent build(){
            return new ChargeEvent(this);
        }
    }
}