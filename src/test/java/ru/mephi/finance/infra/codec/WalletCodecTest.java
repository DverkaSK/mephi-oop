package ru.mephi.finance.infra.codec;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.mephi.finance.domain.Operation;
import ru.mephi.finance.domain.OperationType;
import ru.mephi.finance.domain.Wallet;
import ru.mephi.finance.domain.WalletSnapshot;
import ru.mephi.finance.domain.error.StorageException;
import ru.mephi.finance.service.port.WalletCodec;
import ru.mephi.finance.support.Fixtures;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class WalletCodecTest {

    public static Stream<WalletCodec> codecs() {
        return Stream.of(new JsonWalletCodec(), new CsvWalletCodec());
    }

    @ParameterizedTest
    @MethodSource("codecs")
    public void roundTripKeepsEverything(WalletCodec codec, @TempDir Path dir) {
        Wallet wallet = Fixtures.exampleWallet();
        wallet.addCategory("Пустая, с запятой");
        wallet.addOperation(Operation.create(
                OperationType.EXPENSE, "Еда", new BigDecimal("12.34"), Fixtures.TODAY, "кафе \"Ромашка\", обед"));
        Path file = dir.resolve("nested/wallet." + codec.format());

        codec.write(wallet.snapshot(), file);
        WalletSnapshot restored = codec.read(file);

        assertEquals(wallet.snapshot(), restored);
    }

    @ParameterizedTest
    @MethodSource("codecs")
    public void corruptedFileRaisesStorageException(WalletCodec codec, @TempDir Path dir) throws IOException {
        Path file = Files.writeString(dir.resolve("broken." + codec.format()), "kind,id\nnot,valid,data");

        assertThrows(StorageException.class, () -> codec.read(file));
    }
}
