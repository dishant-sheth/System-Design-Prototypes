package src.models;

import java.util.regex.Pattern;

public class ChargeEvent extends Event {
    final String[] permittedCurrencies = {"USD", "GBP", "INR", "SGD", "EUR"};
    String currency;
    String amount;

    public ChargeEvent(Builder builder){
        this.txn_id = builder.txn_id;
        this.merchant_id = builder.merchant_id;
        this.timestamp = builder.timestamp;
        this.currency = builder.currency;
        this.amount = builder.amount;
    }

    public boolean validateCurrency(){
        boolean isValid = Pattern.matches("^[A-Z]{3}$", currency);
        if(isValid){
            isValid = false;
            for(String permittedCurrency: permittedCurrencies){
                if(permittedCurrency.equals(currency)){
                    isValid = true;
                    break;
                }
            }
        }
        if(!isValid) System.out.println("Invalid " + currency);
        return isValid;
    }

    public boolean validateAmount(){
        boolean isValid = true;
        // no negatives
        if(amount.startsWith("-")) isValid = false;
        if(isValid){
            // Must be decimal with exactly 2 decimal points.
            final String[] amountParts = amount.split("\\.");
            // Is structure valid?
            isValid = (amountParts.length == 2) && (amountParts[1].length() == 2);
            if(isValid){
                isValid = (Double.parseDouble(amount) > 0.0);
            }
        }
        if(!isValid) System.out.println("Invalid " + amount);
        return isValid;
    }

    public boolean isValid(){
        boolean isValid = true;
        isValid &= validateTxnId();
        isValid &= validateMerchantId();
        isValid &= validateTimestamp();
        isValid &= validateCurrency();
        isValid &= validateAmount();
        return isValid;
    }

    public static class Builder {
        
        String txn_id;
        String merchant_id;
        String timestamp;
        String currency;
        String amount;

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

        public Builder amount(String amount){
            this.amount = amount;
            return this;
        }

        public ChargeEvent build(){
            ChargeEvent chargeEvent = new ChargeEvent(this);
            if(chargeEvent.isValid()) return chargeEvent;
            return null;
        }
    }
}