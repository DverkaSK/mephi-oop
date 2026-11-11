package ru.mephi.finance.service;

import ru.mephi.finance.domain.Money;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record Summary(
        String owner,
        OperationFilter filter,
        List<String> categories,
        List<String> missingCategories,
        int operationCount,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        Map<String, BigDecimal> incomeByCategory,
        Map<String, BigDecimal> expenseByCategory,
        List<BudgetStatus> budgets) {

    public Summary {
        categories = List.copyOf(categories);
        missingCategories = List.copyOf(missingCategories);
        incomeByCategory = Collections.unmodifiableMap(new LinkedHashMap<>(incomeByCategory));
        expenseByCategory = Collections.unmodifiableMap(new LinkedHashMap<>(expenseByCategory));
        budgets = List.copyOf(budgets);
    }

    public BigDecimal balance() {
        return totalIncome.subtract(totalExpense);
    }

    public record BudgetStatus(String category, BigDecimal limit, BigDecimal spent) {

        public BigDecimal remaining() {
            return limit.subtract(spent);
        }

        public int percentUsed() {
            return Money.percent(spent, limit);
        }

        public boolean exceeded() {
            return spent.compareTo(limit) > 0;
        }
    }
}
