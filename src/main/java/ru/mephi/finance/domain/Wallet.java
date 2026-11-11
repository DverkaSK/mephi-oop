package ru.mephi.finance.domain;

import ru.mephi.finance.domain.error.NotFoundException;
import ru.mephi.finance.domain.error.ValidationException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class Wallet {

    private final String owner;
    private final List<String> categories = new ArrayList<>();
    private final Map<String, BigDecimal> budgets = new LinkedHashMap<>();
    private final List<Operation> operations = new ArrayList<>();

    public Wallet(String owner) {
        this.owner = Objects.requireNonNull(owner, "owner");
    }

    public static Wallet restore(WalletSnapshot snapshot) {
        Wallet wallet = new Wallet(snapshot.owner());
        wallet.merge(snapshot);
        return wallet;
    }

    public String owner() {
        return owner;
    }

    public List<String> categories() {
        return List.copyOf(categories);
    }

    public Map<String, BigDecimal> budgets() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(budgets));
    }

    public List<Operation> operations() {
        return List.copyOf(operations);
    }

    public Optional<String> findCategory(String name) {
        return categories.stream()
                .filter(category -> category.equalsIgnoreCase(name.strip()))
                .findFirst();
    }

    public String requireCategory(String name) {
        return findCategory(name)
                .orElseThrow(() -> new NotFoundException("Категория «%s» не найдена".formatted(name)));
    }

    public String addCategory(String rawName) {
        String name = CategoryName.validate(rawName);
        if (findCategory(name).isPresent()) {
            throw new ValidationException("Категория «%s» уже существует".formatted(name));
        }
        categories.add(name);
        return name;
    }

    public String ensureCategory(String rawName) {
        String name = CategoryName.validate(rawName);
        return findCategory(name).orElseGet(() -> addCategory(name));
    }

    public String renameCategory(String from, String rawTo) {
        String current = requireCategory(from);
        String target = CategoryName.validate(rawTo);
        findCategory(target)
                .filter(existing -> !existing.equals(current))
                .ifPresent(existing -> {
                    throw new ValidationException("Категория «%s» уже существует".formatted(existing));
                });
        categories.replaceAll(category -> category.equals(current) ? target : category);
        operations.replaceAll(operation -> operation.category().equals(current)
                ? operation.withCategory(target)
                : operation);
        Optional.ofNullable(budgets.remove(current)).ifPresent(limit -> budgets.put(target, limit));
        return target;
    }

    public String removeCategory(String name) {
        String category = requireCategory(name);
        long used = operations.stream().filter(operation -> operation.category().equals(category)).count();
        if (used > 0) {
            throw new ValidationException(
                    "Категорию «%s» нельзя удалить: по ней есть операции (%d). Переименуйте её вместо удаления"
                            .formatted(category, used));
        }
        categories.remove(category);
        budgets.remove(category);
        return category;
    }

    public Optional<BigDecimal> budget(String category) {
        return findCategory(category).map(budgets::get);
    }

    public Optional<BigDecimal> setBudget(String rawCategory, BigDecimal limit) {
        String category = ensureCategory(rawCategory);
        return Optional.ofNullable(budgets.put(category, Money.requirePositive(limit)));
    }

    public BigDecimal removeBudget(String name) {
        String category = requireCategory(name);
        return Optional.ofNullable(budgets.remove(category))
                .orElseThrow(() -> new NotFoundException("Для категории «%s» бюджет не установлен"
                        .formatted(category)));
    }

    public Operation addOperation(Operation operation) {
        String category = ensureCategory(operation.category());
        Operation stored = operation.withCategory(category);
        operations.add(stored);
        return stored;
    }

    public BigDecimal total(OperationType type) {
        return operations.stream()
                .filter(operation -> operation.type() == type)
                .map(Operation::amount)
                .reduce(Money.zero(), BigDecimal::add);
    }

    public BigDecimal spent(String category) {
        return operations.stream()
                .filter(Operation::isExpense)
                .filter(operation -> operation.category().equals(category))
                .map(Operation::amount)
                .reduce(Money.zero(), BigDecimal::add);
    }

    public BigDecimal balance() {
        return operations.stream()
                .map(Operation::signedAmount)
                .reduce(Money.zero(), BigDecimal::add);
    }

    public WalletSnapshot snapshot() {
        return new WalletSnapshot(owner, categories, budgets, operations);
    }

    public MergeResult merge(WalletSnapshot snapshot) {
        Set<UUID> known = operations.stream().map(Operation::id).collect(Collectors.toSet());
        int categoriesBefore = categories.size();
        snapshot.categories().forEach(this::ensureCategory);
        snapshot.budgets().forEach(this::setBudget);
        List<Operation> fresh = snapshot.operations().stream()
                .filter(operation -> !known.contains(operation.id()))
                .map(this::addOperation)
                .toList();
        return new MergeResult(
                fresh.size(),
                snapshot.operations().size() - fresh.size(),
                categories.size() - categoriesBefore,
                snapshot.budgets().size());
    }

    public record MergeResult(int addedOperations, int skippedOperations, int addedCategories, int budgets) {
    }
}
