package src.models;

import java.util.List;

public record EventValidatorResult(
    boolean isValid,
    List<String> validationErrors
){}