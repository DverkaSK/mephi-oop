package ru.mephi.finance.service.port;

import ru.mephi.finance.domain.Wallet;

import java.nio.file.Path;
import java.util.Optional;

public interface WalletRepository {

    Optional<Wallet> load(String owner);

    void save(Wallet wallet);

    Path locationOf(String owner);
}
