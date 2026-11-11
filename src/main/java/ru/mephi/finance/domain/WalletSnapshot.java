package ru.mephi.finance.domain;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record WalletSnapshot(
        String owner,
        List<String> categories,
        Map<String, BigDecimal> budgets,
        List<Operation> operations) {

    public WalletSnapshot {
        Objects.requireNonNull(owner, "owner");
        categories = List.copyOf(categories);
        budgets = Collections.unmodifiableMap(new LinkedHashMap<>(budgets));
        operations = List.copyOf(operations);
    }
}
