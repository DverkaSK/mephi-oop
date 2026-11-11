package ru.mephi.finance.service;

import ru.mephi.finance.domain.Money;
import ru.mephi.finance.domain.Wallet;
import ru.mephi.finance.domain.error.NotFoundException;
import ru.mephi.finance.domain.error.ValidationException;
import ru.mephi.finance.service.format.MoneyFormat;
import ru.mephi.finance.service.port.UserRepository;
import ru.mephi.finance.service.port.WalletRepository;

import java.math.BigDecimal;
import java.util.Objects;

public final class TransferService {

    public static final String CATEGORY = "Переводы";

    private final UserRepository users;
    private final WalletRepository wallets;
    private final OperationService operations;

    public TransferService(UserRepository users, WalletRepository wallets, OperationService operations) {
        this.users = Objects.requireNonNull(users, "users");
        this.wallets = Objects.requireNonNull(wallets, "wallets");
        this.operations = Objects.requireNonNull(operations, "operations");
    }

    public OperationResult transfer(Wallet sender, String recipient, BigDecimal rawAmount, String note) {
        BigDecimal amount = Money.requirePositive(rawAmount);
        if (sender.owner().equals(recipient)) {
            throw new ValidationException("Нельзя перевести деньги самому себе");
        }
        if (!users.exists(recipient)) {
            throw new NotFoundException("Пользователь «%s» не найден".formatted(recipient));
        }
        if (sender.balance().compareTo(amount) < 0) {
            throw new ValidationException("Недостаточно средств: баланс %s, требуется %s".formatted(
                    MoneyFormat.format(sender.balance()), MoneyFormat.format(amount)));
        }
        Wallet target = wallets.load(recipient).orElseGet(() -> new Wallet(recipient));
        String suffix = note == null || note.isBlank() ? "" : ": " + note.strip();
        operations.income(target, CATEGORY, amount, null, "Перевод от " + sender.owner() + suffix);
        OperationResult result = operations.expense(
                sender, CATEGORY, amount, null, "Перевод пользователю " + recipient + suffix);
        wallets.save(target);
        return result;
    }
}
