package ru.mephi.finance.domain;

public enum OperationType {
    INCOME,
    EXPENSE;

    public String title() {
        return switch (this) {
            case INCOME -> "Доход";
            case EXPENSE -> "Расход";
        };
    }
}
