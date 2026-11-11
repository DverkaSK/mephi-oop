package ru.mephi.finance.support;

import ru.mephi.finance.domain.Wallet;
import ru.mephi.finance.service.port.WalletRepository;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class InMemoryWalletRepository implements WalletRepository {

    private final Map<String, Wallet> wallets = new HashMap<>();
    private int saves;

    @Override
    public Optional<Wallet> load(String owner) {
        return Optional.ofNullable(wallets.get(owner));
    }

    @Override
    public void save(Wallet wallet) {
        wallets.put(wallet.owner(), wallet);
        saves++;
    }

    @Override
    public Path locationOf(String owner) {
        return Path.of("memory", owner);
    }

    public int saves() {
        return saves;
    }
}
