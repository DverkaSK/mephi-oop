package ru.mephi.finance.service;

import org.junit.jupiter.api.Test;
import ru.mephi.finance.domain.Wallet;
import ru.mephi.finance.domain.error.ValidationException;
import ru.mephi.finance.support.Fixtures;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class OperationServiceTest {

    private final OperationService operations = new OperationService(new AlertService(), Fixtures.CLOCK);
    private final Wallet wallet = new Wallet("alice");

    @Test
    public void addsOperationWithTodayDateAndReportsNewCategory() {
        OperationResult first = operations.income(wallet, "Зарплата", new BigDecimal("1000"), null, "аванс");
        OperationResult second = operations.income(wallet, "зарплата", new BigDecimal("500"), null, null);

        assertTrue(first.categoryCreated());
        assertFalse(second.categoryCreated());
        assertEquals(Fixtures.TODAY, first.operation().date());
        assertEquals(Fixtures.money("1500"), second.balance());
    }

    @Test
    public void futureDateIsRejected() {
        assertThrows(ValidationException.class, () -> operations.expense(
                wallet, "Еда", BigDecimal.TEN, Fixtures.TODAY.plusDays(1), ""));
        assertTrue(wallet.operations().isEmpty());
    }

    @Test
    public void tooLongNoteIsRejected() {
        String note = "x".repeat(OperationService.MAX_NOTE_LENGTH + 1);

        assertThrows(ValidationException.class, () -> operations.expense(wallet, "Еда", BigDecimal.TEN, null, note));
    }

    @Test
    public void blankCategoryIsRejected() {
        assertThrows(ValidationException.class, () -> operations.expense(wallet, " ", BigDecimal.TEN, null, ""));
    }
}
