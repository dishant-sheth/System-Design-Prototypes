package src.models;

public enum Denominations {
    HUNDRED(100.0),
    FIFTY(50.0),
    TWENTY(20.0),
    TEN(10.0),
    FIVE(5.0);

    double value;
    Denominations(double value){
        this.value = value;
    }

    public double getValue(){
        return this.value;
    }

    public static Denominations getDenomination(double amount){
        for(Denominations denomination: Denominations.values()){
            if(denomination.getValue() == amount) return denomination;
        }
        return null;
    }
}