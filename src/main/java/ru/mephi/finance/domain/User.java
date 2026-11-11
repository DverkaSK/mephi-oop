package ru.mephi.finance.domain;

import java.util.Objects;

public record User(String login, String salt, String passwordHash) {

    public User {
        Objects.requireNonNull(login, "login");
        Objects.requireNonNull(salt, "salt");
        Objects.requireNonNull(passwordHash, "passwordHash");
    }
}
