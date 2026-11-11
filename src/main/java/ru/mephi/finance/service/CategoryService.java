package ru.mephi.finance.service;

import ru.mephi.finance.domain.Wallet;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class CategoryService {

    private final AlertService alerts;

    public CategoryService(AlertService alerts) {
        this.alerts = Objects.requireNonNull(alerts, "alerts");
    }

    public String create(Wallet wallet, String name) {
        return wallet.addCategory(name);
    }

    public String rename(Wallet wallet, String from, String to) {
        return wallet.renameCategory(from, to);
    }

    public String delete(Wallet wallet, String name) {
        return wallet.removeCategory(name);
    }

    public BudgetChange setBudget(Wallet wallet, String category, BigDecimal limit) {
        Optional<BigDecimal> previous = wallet.setBudget(category, limit);
        String resolved = wallet.requireCategory(category);
        BigDecimal current = wallet.budget(resolved).orElseThrow();
        return new BudgetChange(resolved, previous, current, alerts.budget(wallet, resolved).stream().toList());
    }

    public BigDecimal removeBudget(Wallet wallet, String category) {
        return wallet.removeBudget(category);
    }

    public record BudgetChange(String category, Optional<BigDecimal> previous, BigDecimal limit, List<Alert> alerts) {

        public BudgetChange {
            alerts = List.copyOf(alerts);
        }
    }
}
