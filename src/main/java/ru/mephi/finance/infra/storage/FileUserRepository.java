package ru.mephi.finance.infra.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import ru.mephi.finance.domain.User;
import ru.mephi.finance.domain.error.StorageException;
import ru.mephi.finance.infra.FileSupport;
import ru.mephi.finance.service.port.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class FileUserRepository implements UserRepository {

    private final Path file;
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final Map<String, User> users = new LinkedHashMap<>();

    public FileUserRepository(Path file) {
        this.file = Objects.requireNonNull(file, "file").toAbsolutePath();
        load();
    }

    @Override
    public synchronized Optional<User> findByLogin(String login) {
        return Optional.ofNullable(users.get(login));
    }

    @Override
    public synchronized boolean exists(String login) {
        return users.containsKey(login);
    }

    @Override
    public synchronized void save(User user) {
        users.put(user.login(), user);
        persist();
    }

    private void load() {
        if (!Files.isRegularFile(file)) {
            return;
        }
        try {
            Arrays.stream(mapper.readValue(file.toFile(), User[].class))
                    .forEach(user -> users.put(user.login(), user));
        } catch (IOException | RuntimeException e) {
            throw new StorageException("Не удалось прочитать список пользователей " + file, e);
        }
    }

    private void persist() {
        List<User> sorted = users.values().stream().sorted(Comparator.comparing(User::login)).toList();
        try {
            FileSupport.createParent(file);
            mapper.writeValue(file.toFile(), sorted);
        } catch (IOException e) {
            throw new StorageException("Не удалось сохранить список пользователей " + file, e);
        }
    }
}
