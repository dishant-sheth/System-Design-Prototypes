package src.interfaces;

import java.util.List;
import java.util.Set;
import src.models.ChargeEvent;

public interface IFraudDetector {
    Set<String> detectSuspiciousMerchants(List<ChargeEvent> charges);
}