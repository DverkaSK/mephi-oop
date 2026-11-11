package ru.mephi.finance.service;

import ru.mephi.finance.domain.Operation;
import ru.mephi.finance.domain.OperationType;
import ru.mephi.finance.domain.Wallet;
import ru.mephi.finance.domain.error.ValidationException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

public final class OperationService {

    public static final int MAX_NOTE_LENGTH = 120;

    private final AlertService alerts;
    private final Clock clock;

    public OperationService(AlertService alerts, Clock clock) {
        this.alerts = Objects.requireNonNull(alerts, "alerts");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public OperationResult income(Wallet wallet, String category, BigDecimal amount, LocalDate date, String note) {
        return add(wallet, OperationType.INCOME, category, amount, date, note);
    }

    public OperationResult expense(Wallet wallet, String category, BigDecimal amount, LocalDate date, String note) {
        return add(wallet, OperationType.EXPENSE, category, amount, date, note);
    }

    public OperationResult add(
            Wallet wallet, OperationType type, String category, BigDecimal amount, LocalDate date, String note) {
        boolean created = wallet.findCategory(category == null ? "" : category).isEmpty();
        Operation draft = Operation.create(type, category, amount, resolveDate(date), validateNote(note));
        Operation stored = wallet.addOperation(draft);
        return new OperationResult(stored, created, wallet.balance(), alerts.afterOperation(wallet, stored));
    }

    public LocalDate today() {
        return LocalDate.now(clock);
    }

    private LocalDate resolveDate(LocalDate date) {
        LocalDate resolved = Optional.ofNullable(date).orElseGet(this::today);
        if (resolved.isAfter(today())) {
            throw new ValidationException("Дата операции не может быть в будущем: " + resolved);
        }
        return resolved;
    }

    private String validateNote(String note) {
        String value = Optional.ofNullable(note).map(String::strip).orElse("");
        if (value.length() > MAX_NOTE_LENGTH) {
            throw new ValidationException("Комментарий длиннее %d символов".formatted(MAX_NOTE_LENGTH));
        }
        if (value.chars().anyMatch(Character::isISOControl)) {
            throw new ValidationException("Комментарий содержит недопустимые символы");
        }
        return value;
    }
}
