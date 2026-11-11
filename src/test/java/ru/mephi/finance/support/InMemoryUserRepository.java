package ru.mephi.finance.support;

import ru.mephi.finance.domain.User;
import ru.mephi.finance.service.port.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class InMemoryUserRepository implements UserRepository {

    private final Map<String, User> users = new HashMap<>();

    @Override
    public Optional<User> findByLogin(String login) {
        return Optional.ofNullable(users.get(login));
    }

    @Override
    public boolean exists(String login) {
        return users.containsKey(login);
    }

    @Override
    public void save(User user) {
        users.put(user.login(), user);
    }
}
