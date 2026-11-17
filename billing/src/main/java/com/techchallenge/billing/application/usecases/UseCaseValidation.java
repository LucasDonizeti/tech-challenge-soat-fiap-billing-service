package com.techchallenge.billing.application.usecases;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.stream.Collectors;

final class UseCaseValidation {

    private UseCaseValidation() {
    }

    static <T> void validate(T command, Validator validator) {
        var violations = validator.validate(command);
        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Dados inválidos: " + errors);
        }
    }
}
