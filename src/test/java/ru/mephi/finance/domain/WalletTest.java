package ru.mephi.finance.domain;

import org.junit.jupiter.api.Test;
import ru.mephi.finance.domain.error.NotFoundException;
import ru.mephi.finance.domain.error.ValidationException;
import ru.mephi.finance.support.Fixtures;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class WalletTest {

    @Test
    public void calculatesTotalsAndBalance() {
        Wallet wallet = Fixtures.exampleWallet();

        assertEquals(Fixtures.money("63000"), wallet.total(OperationType.INCOME));
        assertEquals(Fixtures.money("8300"), wallet.total(OperationType.EXPENSE));
        assertEquals(Fixtures.money("54700"), wallet.balance());
        assertEquals(Fixtures.money("800"), wallet.spent("Еда"));
    }

    @Test
    public void operationCreatesCategoryAndMatchesExistingIgnoringCase() {
        Wallet wallet = new Wallet("alice");
        wallet.addOperation(Fixtures.expense("Еда", "100"));
        Operation stored = wallet.addOperation(Fixtures.expense("  еДА ", "50"));

        assertEquals(List.of("Еда"), wallet.categories());
        assertEquals("Еда", stored.category());
        assertEquals(Fixtures.money("150"), wallet.spent("Еда"));
    }

    @Test
    public void addingDuplicateCategoryFails() {
        Wallet wallet = new Wallet("alice");
        wallet.addCategory("Еда");

        assertThrows(ValidationException.class, () -> wallet.addCategory("еда"));
    }

    @Test
    public void renameMovesOperationsAndBudget() {
        Wallet wallet = Fixtures.exampleWallet();

        wallet.renameCategory("еда", "Продукты");

        assertTrue(wallet.findCategory("Еда").isEmpty());
        assertEquals(Fixtures.money("800"), wallet.spent("Продукты"));
        assertEquals(Optional.of(Fixtures.money("4000")), wallet.budget("Продукты"));
    }

    @Test
    public void renameToExistingCategoryFails() {
        Wallet wallet = Fixtures.exampleWallet();

        assertThrows(ValidationException.class, () -> wallet.renameCategory("Еда", "Такси"));
    }

    @Test
    public void categoryWithOperationsCannotBeRemoved() {
        Wallet wallet = Fixtures.exampleWallet();
        wallet.addCategory("Подарки");
        wallet.setBudget("Подарки", new BigDecimal("1000"));

        assertThrows(ValidationException.class, () -> wallet.removeCategory("Еда"));
        assertEquals("Подарки", wallet.removeCategory("подарки"));
        assertTrue(wallet.budget("Подарки").isEmpty());
    }

    @Test
    public void budgetMustBePositiveAndCanBeEdited() {
        Wallet wallet = new Wallet("alice");

        assertThrows(ValidationException.class, () -> wallet.setBudget("Еда", BigDecimal.ZERO));
        assertEquals(Optional.empty(), wallet.setBudget("Еда", new BigDecimal("100")));
        assertEquals(Optional.of(Fixtures.money("100")), wallet.setBudget("Еда", new BigDecimal("200")));
        assertEquals(Fixtures.money("200"), wallet.removeBudget("Еда"));
        assertThrows(NotFoundException.class, () -> wallet.removeBudget("Еда"));
    }

    @Test
    public void mergeSkipsKnownOperations() {
        Wallet source = Fixtures.exampleWallet();
        Wallet target = Wallet.restore(source.snapshot());

        Wallet.MergeResult result = target.merge(source.snapshot());

        assertEquals(0, result.addedOperations());
        assertEquals(8, result.skippedOperations());
        assertEquals(source.balance(), target.balance());
    }
}
