package ru.mephi.finance.infra.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.mephi.finance.domain.User;
import ru.mephi.finance.domain.Wallet;
import ru.mephi.finance.infra.codec.JsonWalletCodec;
import ru.mephi.finance.support.Fixtures;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class FileRepositoryTest {

    @Test
    public void usersSurviveRestart(@TempDir Path dir) {
        Path file = dir.resolve("users.json");
        new FileUserRepository(file).save(new User("alice", "salt", "hash"));

        FileUserRepository reopened = new FileUserRepository(file);

        assertTrue(reopened.exists("alice"));
        assertEquals(Optional.of(new User("alice", "salt", "hash")), reopened.findByLogin("alice"));
    }

    @Test
    public void walletIsWrittenToFileAndLoadedByNewRepository(@TempDir Path dir) {
        Wallet wallet = Fixtures.exampleWallet();
        FileWalletRepository repository = new FileWalletRepository(dir, new JsonWalletCodec());

        repository.save(wallet);
        Wallet loaded = new FileWalletRepository(dir, new JsonWalletCodec()).load("alice").orElseThrow();

        assertTrue(Files.isRegularFile(repository.locationOf("alice")));
        assertEquals(wallet.snapshot(), loaded.snapshot());
    }

    @Test
    public void missingWalletIsEmpty(@TempDir Path dir) {
        assertTrue(new FileWalletRepository(dir, new JsonWalletCodec()).load("nobody").isEmpty());
    }
}
