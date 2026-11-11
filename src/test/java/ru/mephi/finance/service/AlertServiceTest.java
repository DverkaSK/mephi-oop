package ru.mephi.finance.service;

import org.junit.jupiter.api.Test;
import ru.mephi.finance.domain.Operation;
import ru.mephi.finance.domain.Wallet;
import ru.mephi.finance.support.Fixtures;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class AlertServiceTest {

    private final AlertService alerts = new AlertService();

    @Test
    public void warnsOnceWhenEightyPercentIsCrossed() {
        Wallet wallet = walletWithIncome();
        wallet.setBudget("Еда", new BigDecimal("1000"));

        List<Alert> crossing = alerts.afterOperation(wallet, wallet.addOperation(Fixtures.expense("Еда", "850")));
        List<Alert> after = alerts.afterOperation(wallet, wallet.addOperation(Fixtures.expense("Еда", "50")));

        assertEquals(AlertLevel.WARNING, crossing.getFirst().level());
        assertTrue(crossing.getFirst().message().contains("85%"));
        assertTrue(after.isEmpty());
    }

    @Test
    public void reportsExhaustedAndExceededBudget() {
        Wallet wallet = walletWithIncome();
        wallet.setBudget("Еда", new BigDecimal("1000"));

        List<Alert> exhausted = alerts.afterOperation(wallet, wallet.addOperation(Fixtures.expense("Еда", "1000")));
        List<Alert> exceeded = alerts.afterOperation(wallet, wallet.addOperation(Fixtures.expense("Еда", "1")));

        assertTrue(exhausted.getFirst().message().contains("исчерпан"));
        assertEquals(AlertLevel.CRITICAL, exceeded.getFirst().level());
        assertTrue(exceeded.getFirst().message().contains("перерасход 1.00"));
    }

    @Test
    public void reportsExpensesAboveIncome() {
        Wallet wallet = new Wallet("alice");
        wallet.addOperation(Fixtures.income("Зарплата", "100"));
        Operation expense = wallet.addOperation(Fixtures.expense("Такси", "150"));

        List<Alert> result = alerts.afterOperation(wallet, expense);

        assertEquals(1, result.size());
        assertTrue(result.getFirst().message().startsWith("Расходы (150.00) превысили доходы (100.00)"));
    }

    @Test
    public void reportsZeroBalance() {
        Wallet wallet = new Wallet("alice");
        wallet.addOperation(Fixtures.income("Зарплата", "100"));
        Operation expense = wallet.addOperation(Fixtures.expense("Такси", "100"));

        assertEquals(List.of(Alert.warning("Баланс кошелька равен нулю")), alerts.afterOperation(wallet, expense));
    }

    @Test
    public void overviewSortsCriticalFirst() {
        List<Alert> overview = alerts.overview(Fixtures.exampleWallet());

        assertEquals(2, overview.size());
        assertEquals(AlertLevel.CRITICAL, overview.get(0).level());
        assertTrue(overview.get(0).message().contains("Коммунальные услуги"));
        assertTrue(overview.get(1).message().contains("Развлечения"));
    }

    private Wallet walletWithIncome() {
        Wallet wallet = new Wallet("alice");
        wallet.addOperation(Fixtures.income("Зарплата", "100000"));
        return wallet;
    }
}
