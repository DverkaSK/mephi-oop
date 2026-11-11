package ru.mephi.finance.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record Operation(UUID id, OperationType type, String category, BigDecimal amount, LocalDate date, String note) {

    public Operation {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(date, "date");
        amount = Money.requirePositive(amount);
        note = note == null ? "" : note.strip();
    }

    public static Operation create(
            OperationType type, String category, BigDecimal amount, LocalDate date, String note) {
        return new Operation(UUID.randomUUID(), type, category, amount, date, note);
    }

    public Operation withCategory(String newCategory) {
        return new Operation(id, type, newCategory, amount, date, note);
    }

    public boolean isIncome() {
        return type == OperationType.INCOME;
    }

    public boolean isExpense() {
        return type == OperationType.EXPENSE;
    }

    public BigDecimal signedAmount() {
        return isIncome() ? amount : amount.negate();
    }
}
