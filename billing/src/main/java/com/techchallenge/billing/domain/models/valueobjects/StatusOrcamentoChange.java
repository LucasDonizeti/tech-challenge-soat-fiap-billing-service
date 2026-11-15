package com.techchallenge.billing.domain.models.valueobjects;

import java.time.LocalDateTime;
import java.util.Objects;

public record StatusOrcamentoChange(StatusOrcamento status, LocalDateTime timestamp) {

    public StatusOrcamentoChange {
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
    }
}
