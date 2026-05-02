package src.models;

import java.util.List;

public class ParseResult{
    public final List<ChargeEvent> charges;
    public final List<DisputeEvent> disputes;
    public final List<String> invalidLines;   // original line, not modified

    public ParseResult(List<ChargeEvent> charges, List<DisputeEvent> disputes, List<String> invalidLines){
        this.charges = charges;
        this.disputes = disputes;
        this.invalidLines = invalidLines;
    }
}