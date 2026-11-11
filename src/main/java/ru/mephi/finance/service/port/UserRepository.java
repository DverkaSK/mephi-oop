package ru.mephi.finance.service.port;

import ru.mephi.finance.domain.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByLogin(String login);

    boolean exists(String login);

    void save(User user);
}
