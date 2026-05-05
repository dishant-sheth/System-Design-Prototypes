package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import src.interfaces.IFraudRule;
import src.models.ParseResult;

public class Main {

    public static void main(String[] args) {
        if(args.length == 0) {
            return;
        }
        String logFile = args[0];
        String path = "src/logs/" + logFile;
        List<String> logs = new ArrayList<>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            String line;
            while((line = bufferedReader.readLine()) != null){
                logs.add(line);
            }   
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        TransactionParser transactionParser = new TransactionParser();
        ParseResult result = transactionParser.parse(logs);
        System.out.println(result.charges.size() + " | " + result.disputes.size() + " | " + result.invalidLines.size());

        // Create the fraud detection rules.
        HighVelocityFraudRule highVelocityFraudRule = new HighVelocityFraudRule(60, 3);
        HighValueBurstRule highValueBurstRule = new HighValueBurstRule(60, new BigDecimal("100"));
        List<IFraudRule> fraudRules = new ArrayList<>();
        fraudRules.add(highVelocityFraudRule);
        fraudRules.add(highValueBurstRule);
        FraudDetector fraudDetector = new FraudDetector(fraudRules);
        Set<String> errors = fraudDetector.detectSuspiciousMerchants(result.charges);
        System.out.println(errors);
    }
}