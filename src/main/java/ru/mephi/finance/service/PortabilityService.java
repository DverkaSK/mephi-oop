package ru.mephi.finance.service;

import ru.mephi.finance.domain.Wallet;
import ru.mephi.finance.domain.WalletSnapshot;
import ru.mephi.finance.domain.error.NotFoundException;
import ru.mephi.finance.domain.error.ValidationException;
import ru.mephi.finance.service.port.WalletCodec;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class PortabilityService {

    private final Map<String, WalletCodec> codecs;

    public PortabilityService(List<WalletCodec> codecs) {
        this.codecs = codecs.stream().collect(Collectors.toMap(WalletCodec::format, Function.identity()));
    }

    public List<String> formats() {
        return codecs.keySet().stream().sorted().toList();
    }

    public Path export(Wallet wallet, String format, Path file) {
        codec(format).write(wallet.snapshot(), file);
        return file.toAbsolutePath();
    }

    public Wallet.MergeResult importInto(Wallet wallet, String format, Path file) {
        if (!Files.isRegularFile(file)) {
            throw new NotFoundException("Файл не найден: " + file.toAbsolutePath());
        }
        WalletSnapshot snapshot = codec(format).read(file);
        return wallet.merge(snapshot);
    }

    public String detectFormat(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        return formats().stream()
                .filter(format -> name.endsWith("." + format))
                .findFirst()
                .orElseThrow(() -> new ValidationException(
                        "Не удалось определить формат по имени файла, поддерживаются: "
                                + String.join(", ", formats())));
    }

    private WalletCodec codec(String format) {
        WalletCodec codec = codecs.get(format.toLowerCase(Locale.ROOT));
        if (codec == null) {
            throw new ValidationException("Формат «%s» не поддерживается, доступны: %s"
                    .formatted(format, String.join(", ", formats())));
        }
        return codec;
    }
}
