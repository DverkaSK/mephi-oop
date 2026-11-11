package ru.mephi.finance.infra.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import ru.mephi.finance.domain.WalletSnapshot;
import ru.mephi.finance.domain.error.StorageException;
import ru.mephi.finance.infra.FileSupport;
import ru.mephi.finance.service.port.WalletCodec;

import java.io.IOException;
import java.nio.file.Path;

public final class JsonWalletCodec implements WalletCodec {

    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    public String format() {
        return "json";
    }

    @Override
    public void write(WalletSnapshot snapshot, Path file) {
        try {
            FileSupport.createParent(file);
            mapper.writeValue(file.toFile(), WalletDocument.from(snapshot));
        } catch (IOException e) {
            throw new StorageException("Не удалось записать файл " + file.toAbsolutePath(), e);
        }
    }

    @Override
    public WalletSnapshot read(Path file) {
        try {
            return mapper.readValue(file.toFile(), WalletDocument.class).toSnapshot();
        } catch (IOException | RuntimeException e) {
            throw new StorageException(
                    "Файл %s повреждён или имеет неверный формат".formatted(file.toAbsolutePath()), e);
        }
    }
}
