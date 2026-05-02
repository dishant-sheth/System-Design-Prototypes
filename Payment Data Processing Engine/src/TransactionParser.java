package src;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import src.interfaces.IEventParser;
import src.models.*;

public class TransactionParser implements IEventParser {

    private final String CHARGE = "CHARGE";
    private final String DISPUTE = "DISPUTE";

    private static final ChargeEventValidator CHARGE_VALIDATOR = new ChargeEventValidator();
    private static final DisputeEventValidator DISPUTE_VALIDATOR = new DisputeEventValidator();

    @Override
    public ParseResult parse(List<String> rawLines){
        final List<ChargeEvent> charges = new ArrayList<>();
        final List<DisputeEvent> disputes = new ArrayList<>();
        final List<String> invalidLines = new ArrayList<>();

        for(String line: rawLines){
            // Remove leading or ending white spaces.
            final String trimmedLine = line.trim();
            // Split line for easy parsing.
            final String[] logParts = trimmedLine.split(" ");
            final String eventType = logParts[0];
            int idxToParse = 0;

            switch (eventType) {
                case CHARGE -> {
                    // Needs atleast 6 parts.
                    if(logParts.length >= 6){
                        // Txn ID
                        idxToParse = getNextValidPart(idxToParse + 1, logParts);
                        final String txnId = getPart(idxToParse, logParts);
                        // Merchant ID
                        idxToParse = getNextValidPart(idxToParse + 1, logParts);
                        final String merchantId = getPart(idxToParse, logParts);
                        // Currency
                        idxToParse = getNextValidPart(idxToParse + 1, logParts);
                        final String currency = getPart(idxToParse, logParts);
                        // Amount
                        idxToParse = getNextValidPart(idxToParse + 1, logParts);
                        final BigDecimal amount =  new BigDecimal(getPart(idxToParse, logParts));
                        // Timestamp
                        idxToParse = getNextValidPart(idxToParse + 1, logParts);
                        final String timestamp = getPart(idxToParse, logParts);

                        // Extra parts?
                        idxToParse = getNextValidPart(idxToParse + 1, logParts);
                        final String extras = getPart(idxToParse, logParts);
                        if(extras.isEmpty()) {
                            // Build the event.
                            ChargeEvent event = ChargeEvent.Builder.newInstance()
                            .txnId(txnId)
                            .merchantId(merchantId)
                            .currency(currency)
                            .amount(amount)
                            .timestamp(timestamp)
                            .build();

                            EventValidatorResult result = CHARGE_VALIDATOR.validate(event);
                            if(result.isValid()) {
                                charges.add(event);
                                System.out.println("CHARGE processed successfully");
                                continue;
                            }
                            System.out.println("[ERROR] " + result.validationErrors());
                        }
                    }
                    invalidLines.add(line);
                }
                case DISPUTE -> {
                    if(logParts.length >= 5){
                        // Txn ID
                        idxToParse = getNextValidPart(idxToParse + 1, logParts);
                        final String txnId = getPart(idxToParse, logParts);
                        // Merchant ID
                        idxToParse = getNextValidPart(idxToParse + 1, logParts);
                        final String merchantId = getPart(idxToParse, logParts);
                        // Reason code
                        idxToParse = getNextValidPart(idxToParse + 1, logParts);
                        final String reasonCode = getPart(idxToParse, logParts);
                        // Timestamp
                        idxToParse = getNextValidPart(idxToParse + 1, logParts);
                        final String timestamp = getPart(idxToParse, logParts);

                        // Extra parts?
                        idxToParse = getNextValidPart(idxToParse + 1, logParts);
                        final String extras = getPart(idxToParse, logParts);
                        if(extras.isEmpty()) {
                            DisputeEvent event = DisputeEvent.Builder.newInstance()
                            .txnId(txnId)
                            .merchantId(merchantId)
                            .reasonCode(reasonCode)
                            .timestamp(timestamp)
                            .build();
    
                            EventValidatorResult result = DISPUTE_VALIDATOR.validate(event);
                            if(result.isValid()){
                                disputes.add(event);
                                System.out.println("DISPUTE processed successfully");
                                continue;
                            }
                            System.out.println("[ERROR] " + result.validationErrors());
                        }
                    }
                    invalidLines.add(line);
                }
                default -> invalidLines.add(line);
            }
        }

        return new ParseResult(charges, disputes, invalidLines);
    }

    private String getPart(int idx, String[] logParts){
        if(idx >= logParts.length) return "";
        else return logParts[idx];
    }

    private int getNextValidPart(int currIdx, String[] logParts){
        while(currIdx < logParts.length){
            if(isPartValid(logParts[currIdx])) break;
            ++currIdx;
        }
        return currIdx;
    }

    private boolean isPartValid(String part){
        part = part.trim();
        return (part.length() > 0);
    }

}