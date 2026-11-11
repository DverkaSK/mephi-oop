package ru.mephi.finance.service.port;

import ru.mephi.finance.domain.WalletSnapshot;

import java.nio.file.Path;

public interface WalletCodec {

    String format();

    void write(WalletSnapshot snapshot, Path file);

    WalletSnapshot read(Path file);
}
