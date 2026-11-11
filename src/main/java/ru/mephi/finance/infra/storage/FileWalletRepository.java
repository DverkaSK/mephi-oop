package ru.mephi.finance.infra.storage;

import ru.mephi.finance.domain.Wallet;
import ru.mephi.finance.service.port.WalletCodec;
import ru.mephi.finance.service.port.WalletRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class FileWalletRepository implements WalletRepository {

    private final Path directory;
    private final WalletCodec codec;
    private final Map<String, Wallet> cache = new HashMap<>();

    public FileWalletRepository(Path directory, WalletCodec codec) {
        this.directory = Objects.requireNonNull(directory, "directory").toAbsolutePath();
        this.codec = Objects.requireNonNull(codec, "codec");
    }

    @Override
    public synchronized Optional<Wallet> load(String owner) {
        return Optional.ofNullable(cache.get(owner)).or(() -> readFromDisk(owner));
    }

    @Override
    public synchronized void save(Wallet wallet) {
        cache.put(wallet.owner(), wallet);
        codec.write(wallet.snapshot(), locationOf(wallet.owner()));
    }

    @Override
    public Path locationOf(String owner) {
        return directory.resolve(owner + "." + codec.format());
    }

    private Optional<Wallet> readFromDisk(String owner) {
        Path file = locationOf(owner);
        if (!Files.isRegularFile(file)) {
            return Optional.empty();
        }
        Wallet wallet = Wallet.restore(codec.read(file));
        cache.put(owner, wallet);
        return Optional.of(wallet);
    }
}
