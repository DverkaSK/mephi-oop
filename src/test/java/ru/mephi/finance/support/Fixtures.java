package ru.mephi.finance.support;

import ru.mephi.finance.domain.Operation;
import ru.mephi.finance.domain.OperationType;
import ru.mephi.finance.domain.Wallet;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public final class Fixtures {

    public static final LocalDate TODAY = LocalDate.of(2026, 11, 11);
    public static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-11T10:00:00Z"), ZoneOffset.UTC);

    private Fixtures() {
    }

    public static BigDecimal money(String value) {
        return new BigDecimal(value).setScale(2);
    }

    public static Operation income(String category, String amount) {
        return Operation.create(OperationType.INCOME, category, new BigDecimal(amount), TODAY, "");
    }

    public static Operation expense(String category, String amount) {
        return Operation.create(OperationType.EXPENSE, category, new BigDecimal(amount), TODAY, "");
    }

    public static Operation expense(String category, String amount, LocalDate date) {
        return Operation.create(OperationType.EXPENSE, category, new BigDecimal(amount), date, "");
    }

    public static Wallet exampleWallet() {
        Wallet wallet = new Wallet("alice");
        wallet.addOperation(expense("Еда", "300"));
        wallet.addOperation(expense("Еда", "500"));
        wallet.addOperation(expense("Развлечения", "3000"));
        wallet.addOperation(expense("Коммунальные услуги", "3000"));
        wallet.addOperation(expense("Такси", "1500"));
        wallet.addOperation(income("Зарплата", "20000"));
        wallet.addOperation(income("Зарплата", "40000"));
        wallet.addOperation(income("Бонус", "3000"));
        wallet.setBudget("Еда", new BigDecimal("4000"));
        wallet.setBudget("Развлечения", new BigDecimal("3000"));
        wallet.setBudget("Коммунальные услуги", new BigDecimal("2500"));
        return wallet;
    }
}
