package ru.mephi.finance.service;

import ru.mephi.finance.domain.Operation;
import ru.mephi.finance.domain.error.ValidationException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public record OperationFilter(List<String> categories, Optional<LocalDate> from, Optional<LocalDate> to) {

    public OperationFilter {
        categories = List.copyOf(categories);
        if (from.isPresent() && to.isPresent() && from.get().isAfter(to.get())) {
            throw new ValidationException("Начало периода %s позже его конца %s".formatted(from.get(), to.get()));
        }
    }

    public static OperationFilter all() {
        return new OperationFilter(List.of(), Optional.empty(), Optional.empty());
    }

    public boolean hasCategories() {
        return !categories.isEmpty();
    }

    public boolean hasPeriod() {
        return from.isPresent() || to.isPresent();
    }

    public boolean inPeriod(Operation operation) {
        return from.map(start -> !operation.date().isBefore(start)).orElse(true)
                && to.map(end -> !operation.date().isAfter(end)).orElse(true);
    }
}
