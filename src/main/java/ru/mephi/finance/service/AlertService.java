package ru.mephi.finance.service;

import ru.mephi.finance.domain.Money;
import ru.mephi.finance.domain.Operation;
import ru.mephi.finance.domain.OperationType;
import ru.mephi.finance.domain.Wallet;
import ru.mephi.finance.service.format.MoneyFormat;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class AlertService {

    public static final int WARNING_PERCENT = 80;

    public List<Alert> afterOperation(Wallet wallet, Operation operation) {
        Stream<Alert> budgetAlerts = operation.isExpense()
                ? budgetAfterExpense(wallet, operation).stream()
                : Stream.empty();
        return sorted(Stream.concat(budgetAlerts, balanceAlerts(wallet)));
    }

    public List<Alert> overview(Wallet wallet) {
        Stream<Alert> budgetAlerts = wallet.budgets().entrySet().stream()
                .map(entry -> budgetState(entry.getKey(), entry.getValue(), wallet.spent(entry.getKey())))
                .flatMap(Optional::stream);
        return sorted(Stream.concat(budgetAlerts, balanceAlerts(wallet)));
    }

    public Optional<Alert> budget(Wallet wallet, String category) {
        return wallet.budget(category).flatMap(limit -> budgetState(category, limit, wallet.spent(category)));
    }

    private Optional<Alert> budgetAfterExpense(Wallet wallet, Operation operation) {
        return wallet.budget(operation.category()).flatMap(limit -> {
            BigDecimal spent = wallet.spent(operation.category());
            BigDecimal before = spent.subtract(operation.amount());
            boolean crossedWarning = Money.percent(before, limit) < WARNING_PERCENT
                    && Money.percent(spent, limit) >= WARNING_PERCENT;
            return spent.compareTo(limit) >= 0 || crossedWarning
                    ? budgetState(operation.category(), limit, spent)
                    : Optional.empty();
        });
    }

    private Optional<Alert> budgetState(String category, BigDecimal limit, BigDecimal spent) {
        int compare = spent.compareTo(limit);
        if (compare > 0) {
            return Optional.of(Alert.critical("Превышен бюджет «%s»: потрачено %s из %s, перерасход %s".formatted(
                    category, MoneyFormat.format(spent), MoneyFormat.format(limit),
                    MoneyFormat.format(spent.subtract(limit)))));
        }
        if (compare == 0) {
            return Optional.of(Alert.warning("Бюджет «%s» исчерпан полностью: %s".formatted(
                    category, MoneyFormat.format(limit))));
        }
        int percent = Money.percent(spent, limit);
        if (percent >= WARNING_PERCENT) {
            return Optional.of(Alert.warning("Использовано %d%% бюджета «%s», осталось %s".formatted(
                    percent, category, MoneyFormat.format(limit.subtract(spent)))));
        }
        return Optional.empty();
    }

    private Stream<Alert> balanceAlerts(Wallet wallet) {
        BigDecimal income = wallet.total(OperationType.INCOME);
        BigDecimal expense = wallet.total(OperationType.EXPENSE);
        int compare = expense.compareTo(income);
        if (compare > 0) {
            return Stream.of(Alert.critical("Расходы (%s) превысили доходы (%s), баланс %s".formatted(
                    MoneyFormat.format(expense), MoneyFormat.format(income), MoneyFormat.format(wallet.balance()))));
        }
        if (compare == 0 && expense.signum() > 0) {
            return Stream.of(Alert.warning("Баланс кошелька равен нулю"));
        }
        return Stream.empty();
    }

    private List<Alert> sorted(Stream<Alert> alerts) {
        return alerts.sorted(Comparator.comparing(Alert::level)).toList();
    }
}
