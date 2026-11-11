package ru.mephi.finance.service;

import ru.mephi.finance.domain.Money;
import ru.mephi.finance.domain.Operation;
import ru.mephi.finance.domain.OperationType;
import ru.mephi.finance.domain.Wallet;
import ru.mephi.finance.domain.error.NotFoundException;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class StatisticsService {

    public Summary summarize(Wallet wallet, OperationFilter filter) {
        Selection selection = select(wallet, filter);
        List<Operation> operations = selection.operations();
        Predicate<String> budgetVisible = category -> !filter.hasCategories()
                || selection.categories().contains(category);
        List<Summary.BudgetStatus> budgets = wallet.budgets().entrySet().stream()
                .filter(entry -> budgetVisible.test(entry.getKey()))
                .map(entry -> new Summary.BudgetStatus(
                        entry.getKey(), entry.getValue(), sum(operations, OperationType.EXPENSE, entry.getKey())))
                .sorted(Comparator.comparing(Summary.BudgetStatus::remaining))
                .toList();
        return new Summary(
                wallet.owner(),
                filter,
                selection.categories(),
                selection.missing(),
                operations.size(),
                total(operations, OperationType.INCOME),
                total(operations, OperationType.EXPENSE),
                byCategory(operations, OperationType.INCOME),
                byCategory(operations, OperationType.EXPENSE),
                budgets);
    }

    public Selection select(Wallet wallet, OperationFilter filter) {
        Map<Boolean, List<String>> resolved = filter.categories().stream()
                .distinct()
                .collect(Collectors.partitioningBy(name -> wallet.findCategory(name).isPresent()));
        List<String> found = resolved.get(true).stream()
                .map(wallet::requireCategory)
                .distinct()
                .toList();
        List<String> missing = resolved.get(false);
        if (filter.hasCategories() && found.isEmpty()) {
            throw new NotFoundException("Категории не найдены: " + String.join(", ", missing));
        }
        List<Operation> operations = wallet.operations().stream()
                .filter(filter::inPeriod)
                .filter(operation -> found.isEmpty() || found.contains(operation.category()))
                .sorted(Comparator.comparing(Operation::date))
                .toList();
        return new Selection(found, missing, operations);
    }

    private BigDecimal total(List<Operation> operations, OperationType type) {
        return operations.stream()
                .filter(operation -> operation.type() == type)
                .map(Operation::amount)
                .reduce(Money.zero(), BigDecimal::add);
    }

    private BigDecimal sum(List<Operation> operations, OperationType type, String category) {
        return operations.stream()
                .filter(operation -> operation.type() == type && operation.category().equals(category))
                .map(Operation::amount)
                .reduce(Money.zero(), BigDecimal::add);
    }

    private Map<String, BigDecimal> byCategory(List<Operation> operations, OperationType type) {
        return operations.stream()
                .filter(operation -> operation.type() == type)
                .collect(Collectors.groupingBy(
                        Operation::category,
                        Collectors.reducing(Money.zero(), Operation::amount, BigDecimal::add)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, BigDecimal::add, LinkedHashMap::new));
    }

    public record Selection(List<String> categories, List<String> missing, List<Operation> operations) {

        public Selection {
            categories = List.copyOf(categories);
            missing = List.copyOf(missing);
            operations = List.copyOf(operations);
        }
    }
}
