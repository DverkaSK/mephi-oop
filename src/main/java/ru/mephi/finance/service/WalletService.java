package ru.mephi.finance.service;

import ru.mephi.finance.domain.Wallet;
import ru.mephi.finance.service.port.WalletRepository;

import java.nio.file.Path;
import java.util.Objects;

public final class WalletService {

    private final WalletRepository wallets;

    public WalletService(WalletRepository wallets) {
        this.wallets = Objects.requireNonNull(wallets, "wallets");
    }

    public Wallet open(String owner) {
        return wallets.load(owner).orElseGet(() -> new Wallet(owner));
    }

    public Path save(Wallet wallet) {
        wallets.save(wallet);
        return wallets.locationOf(wallet.owner());
    }
}
