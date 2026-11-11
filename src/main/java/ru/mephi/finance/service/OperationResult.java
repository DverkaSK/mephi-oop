package ru.mephi.finance.service;

import ru.mephi.finance.domain.Operation;

import java.math.BigDecimal;
import java.util.List;

public record OperationResult(Operation operation, boolean categoryCreated, BigDecimal balance, List<Alert> alerts) {

    public OperationResult {
        alerts = List.copyOf(alerts);
    }
}
