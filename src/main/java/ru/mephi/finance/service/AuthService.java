package ru.mephi.finance.service;

import ru.mephi.finance.domain.User;
import ru.mephi.finance.domain.error.AuthenticationException;
import ru.mephi.finance.domain.error.ValidationException;
import ru.mephi.finance.service.port.PasswordHasher;
import ru.mephi.finance.service.port.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.regex.Pattern;

public final class AuthService {

    private static final Pattern LOGIN = Pattern.compile("[\\p{L}\\p{N}_.-]{3,32}");
    private static final int MIN_PASSWORD_LENGTH = 4;
    private static final int MAX_PASSWORD_LENGTH = 64;

    private final UserRepository users;
    private final PasswordHasher hasher;

    public AuthService(UserRepository users, PasswordHasher hasher) {
        this.users = Objects.requireNonNull(users, "users");
        this.hasher = Objects.requireNonNull(hasher, "hasher");
    }

    public User register(String login, String password) {
        validateLogin(login);
        validatePassword(password);
        if (users.exists(login)) {
            throw new ValidationException("Пользователь «%s» уже зарегистрирован".formatted(login));
        }
        String salt = hasher.newSalt();
        User user = new User(login, salt, hasher.hash(password, salt));
        users.save(user);
        return user;
    }

    public User authenticate(String login, String password) {
        return users.findByLogin(login)
                .filter(user -> matches(user, password))
                .orElseThrow(() -> new AuthenticationException("Неверный логин или пароль"));
    }

    public boolean exists(String login) {
        return users.exists(login);
    }

    private boolean matches(User user, String password) {
        byte[] expected = user.passwordHash().getBytes(StandardCharsets.UTF_8);
        byte[] actual = hasher.hash(password, user.salt()).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual);
    }

    private void validateLogin(String login) {
        if (login == null || !LOGIN.matcher(login).matches()) {
            throw new ValidationException(
                    "Логин должен состоять из 3-32 символов: буквы, цифры, знаки _ . -");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new ValidationException("Пароль не может быть пустым");
        }
        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            throw new ValidationException("Длина пароля должна быть от %d до %d символов"
                    .formatted(MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH));
        }
    }
}
