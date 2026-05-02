package src.models;

import java.util.regex.Pattern;

public abstract class Event {
    String txn_id;
    String merchant_id;
    String timestamp;

    // Must match "txn_[A-Z0-9]{8}"
    public boolean validateTxnId(){
        final boolean isValid = Pattern.matches("^txn_[A-Z0-9]{8}$", txn_id);
        if(!isValid) System.out.println("Invalid " + txn_id);
        return isValid;
    }

    public boolean validateMerchantId(){
        final boolean isValid = Pattern.matches("^merch_[a-z]+", merchant_id);
        if(!isValid) System.out.println("Invalid " + merchant_id);
        return isValid;
    }

    // Format needed = YYYY-MM-DDTHH:MM:SS
    public boolean validateTimestamp(){
        final String dateRegexPattern = "^[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|1[0-9]|2[0-9]|3[0-1])";
        final String timeRegexPatter = "T(0[0-9]|1[0-9]|2[0-3]):[0-5]{1}[0-9]{1}:[0-5]{1}[0-9]{1}$";
        final boolean isValid = Pattern.matches(dateRegexPattern + timeRegexPatter, timestamp);
        if(!isValid) System.out.println("Invalid " + timestamp);
        return isValid;
    }

}