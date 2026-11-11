package ru.mephi.finance.infra;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class FileSupport {

    private FileSupport() {
    }

    public static void createParent(Path file) throws IOException {
        Optional<Path> parent = Optional.ofNullable(file.toAbsolutePath().getParent());
        if (parent.isPresent()) {
            Files.createDirectories(parent.get());
        }
    }
}
