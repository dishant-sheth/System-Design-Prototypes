package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
            e.printStackTrace();
        }

        TransactionParser transactionParser = new TransactionParser();
        ParseResult result = transactionParser.parse(logs);
        System.out.println(result.charges.size() + " | " + result.disputes.size() + " | " + result.invalidLines.size());
    }
}