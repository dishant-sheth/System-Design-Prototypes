package src.interfaces;

import java.util.regex.Pattern;
import src.models.Event;
import src.models.EventValidatorResult;

public interface IEventValidator {
    final static Pattern txnIdPattern = Pattern.compile("^txn_[A-Z0-9]{8}$");
    final static Pattern merchantIdPattern = Pattern.compile("^merch_[a-z]+$");

    final static String dateRegexPattern = "^[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|1[0-9]|2[0-9]|3[0-1])";
    final static String timeRegexPatter = "T(0[0-9]|1[0-9]|2[0-3]):[0-5]{1}[0-9]{1}:[0-5]{1}[0-9]{1}$";
    final static Pattern timestampPattern = Pattern.compile(dateRegexPattern + timeRegexPatter);

    public EventValidatorResult validate(Event event);
}