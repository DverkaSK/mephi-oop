package ru.mephi.finance.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.mephi.finance.domain.Operation;
import ru.mephi.finance.domain.User;
import ru.mephi.finance.domain.Wallet;
import ru.mephi.finance.domain.error.NotFoundException;
import ru.mephi.finance.domain.error.ValidationException;
import ru.mephi.finance.support.Fixtures;
import ru.mephi.finance.support.InMemoryUserRepository;
import ru.mephi.finance.support.InMemoryWalletRepository;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class TransferServiceTest {

    private final InMemoryUserRepository users = new InMemoryUserRepository();
    private final InMemoryWalletRepository wallets = new InMemoryWalletRepository();
    private final TransferService transfers = new TransferService(
            users, wallets, new OperationService(new AlertService(), Fixtures.CLOCK));
    private final Wallet alice = new Wallet("alice");

    @BeforeEach
    public void setUp() {
        users.save(new User("alice", "salt", "hash"));
        users.save(new User("bob", "salt", "hash"));
        alice.addOperation(Fixtures.income("Зарплата", "1000"));
    }

    @Test
    public void recordsExpenseForSenderAndIncomeForRecipient() {
        transfers.transfer(alice, "bob", new BigDecimal("300"), "за обед");

        Wallet bob = wallets.load("bob").orElseThrow();
        Operation received = bob.operations().getFirst();
        assertEquals(Fixtures.money("700"), alice.balance());
        assertEquals(Fixtures.money("300"), bob.balance());
        assertEquals(TransferService.CATEGORY, received.category());
        assertTrue(received.note().contains("alice"));
        assertEquals(1, wallets.saves());
    }

    @Test
    public void insufficientFundsAreRejected() {
        assertThrows(ValidationException.class, () -> transfers.transfer(alice, "bob", new BigDecimal("1001"), ""));
        assertTrue(wallets.load("bob").isEmpty());
    }

    @Test
    public void unknownRecipientAndSelfTransferAreRejected() {
        assertThrows(NotFoundException.class, () -> transfers.transfer(alice, "carol", BigDecimal.TEN, ""));
        assertThrows(ValidationException.class, () -> transfers.transfer(alice, "alice", BigDecimal.TEN, ""));
        assertEquals(1, alice.operations().size());
    }
}
