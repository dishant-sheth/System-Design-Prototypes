package src.models;

import java.time.Instant;
import java.time.LocalDateTime;

public class Transaction {
    final Product product;
    final long timestamp;
    public double amountPaid;
    public double changeReturned;

    public Transaction(Product product){
        this.product = product;
        this.timestamp = System.currentTimeMillis();
    }

    public void setAmountPaid(double amount){
        this.amountPaid = amount;
    }

    public void setChangeReturned(double amount){
        this.changeReturned = amount;
    }

    public Product getProduct(){
        return this.product;
    }

    @Override
    public String toString(){
        final String strTimestamp = Instant.ofEpochMilli(this.timestamp).toString();
        return "<<" + ("Product Name: " + this.product.name + "\n") + 
            ("Amount Paid: " + this.amountPaid + "\n") + 
            ("Change Returned: " + this.changeReturned + "\n") + 
            ("Timestamp: " + LocalDateTime.parse(strTimestamp).toString() + "\n");
    }
}