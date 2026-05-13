package src;

import java.util.HashMap;
import java.util.Map;
import src.exceptions.CannotFormChangeException;
import src.models.Denominations;

public class ChangeDispenser {
    
    private final Map<Denominations, Integer> changeMap;

    public ChangeDispenser(){
        this.changeMap = new HashMap<>();
        for(Denominations denomination: Denominations.values()){
            changeMap.put(denomination, 0);
        }
    }

    public void refillChange(Map<Denominations, Integer> refillMap){
        for(Denominations denomination: refillMap.keySet()){
            addChange(denomination, refillMap.get(denomination));
        }
    }

    public void returnChange(final double amount){
        Map<Denominations, Integer> returnChangeMap = new HashMap<>();
        if(!changeCalculator(amount, returnChangeMap)) {
            // Throw exception if change cannot be formed.
            throw new CannotFormChangeException();
        }
        System.out.println(returnChangeMap);
        for(Denominations denomination: returnChangeMap.keySet()){
            removeChange(denomination, returnChangeMap.get(denomination));
        }
    }

    public void addChange(final Denominations denomination, Integer quantity){
        this.changeMap.put(denomination, changeMap.get(denomination) + quantity);
    }

    private void removeChange(final Denominations denomination, Integer quantity){
        this.changeMap.put(denomination, changeMap.get(denomination) - quantity);
    }

    private boolean changeCalculator(double amount, Map<Denominations, Integer> result){
        if(amount < 0.0) return false;
        if(amount == 0.0) return true;
        for(Denominations denomination: Denominations.values()){
            // Check if we can use it.
            if(changeMap.get(denomination) >= (result.getOrDefault(denomination, 0) + 1)){
                // Update result map.
                if(!result.containsKey(denomination)){
                    result.put(denomination, 0);
                }
                result.put(denomination, result.get(denomination) + 1);

                if(changeCalculator(amount - denomination.getValue(), result)) return true;

                // Remove entry from result map.
                if(result.get(denomination) == 1){
                    result.remove(denomination);
                } else {
                    result.put(denomination, result.get(denomination) - 1);
                }
            }
        }
        return false;
    }
}