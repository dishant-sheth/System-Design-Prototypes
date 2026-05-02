package src.interfaces;

import java.util.List;
import src.models.ParseResult;

public interface IEventParser {
    ParseResult parse(List<String> rawLines);
}