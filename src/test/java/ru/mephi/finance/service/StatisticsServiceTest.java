package ru.mephi.finance.service;

import org.junit.jupiter.api.Test;
import ru.mephi.finance.domain.Wallet;
import ru.mephi.finance.domain.error.NotFoundException;
import ru.mephi.finance.domain.error.ValidationException;
import ru.mephi.finance.support.Fixtures;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class StatisticsServiceTest {

    private final StatisticsService statistics = new StatisticsService();

    @Test
    public void reproducesExampleFromAssignment() {
        Summary summary = statistics.summarize(Fixtures.exampleWallet(), OperationFilter.all());

        assertEquals(Fixtures.money("63000"), summary.totalIncome());
        assertEquals(Fixtures.money("8300"), summary.totalExpense());
        assertEquals(Fixtures.money("60000"), summary.incomeByCategory().get("Зарплата"));
        Map<String, String> remaining = summary.budgets().stream().collect(Collectors.toMap(
                Summary.BudgetStatus::category, status -> status.remaining().toPlainString()));
        assertEquals(Map.of("Коммунальные услуги", "-500.00", "Еда", "3200.00", "Развлечения", "0.00"), remaining);
    }

    @Test
    public void countsSeveralCategoriesAndReportsMissingOnes() {
        OperationFilter filter = new OperationFilter(
                List.of("еда", "Такси", "Кино"), Optional.empty(), Optional.empty());

        Summary summary = statistics.summarize(Fixtures.exampleWallet(), filter);

        assertEquals(List.of("Еда", "Такси"), summary.categories());
        assertEquals(List.of("Кино"), summary.missingCategories());
        assertEquals(Fixtures.money("2300"), summary.totalExpense());
        assertEquals(1, summary.budgets().size());
    }

    @Test
    public void failsWhenNoRequestedCategoryExists() {
        OperationFilter filter = new OperationFilter(List.of("Кино"), Optional.empty(), Optional.empty());

        assertThrows(NotFoundException.class, () -> statistics.summarize(Fixtures.exampleWallet(), filter));
    }

    @Test
    public void filtersByPeriod() {
        Wallet wallet = new Wallet("alice");
        wallet.addOperation(Fixtures.expense("Еда", "100", Fixtures.TODAY.minusMonths(1)));
        wallet.addOperation(Fixtures.expense("Еда", "200", Fixtures.TODAY));
        OperationFilter filter = new OperationFilter(
                List.of(), Optional.of(Fixtures.TODAY.withDayOfMonth(1)), Optional.empty());

        Summary summary = statistics.summarize(wallet, filter);

        assertEquals(1, summary.operationCount());
        assertEquals(Fixtures.money("200"), summary.totalExpense());
    }

    @Test
    public void invertedPeriodIsRejected() {
        assertThrows(ValidationException.class, () -> new OperationFilter(
                List.of(), Optional.of(Fixtures.TODAY), Optional.of(Fixtures.TODAY.minusDays(1))));
    }
}
