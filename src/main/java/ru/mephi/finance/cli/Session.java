package ru.mephi.finance.cli;

import ru.mephi.finance.domain.User;
import ru.mephi.finance.domain.Wallet;

import java.util.Objects;
import java.util.Optional;

public final class Session {

    private User user;
    private Wallet wallet;

    public void start(User newUser, Wallet newWallet) {
        this.user = Objects.requireNonNull(newUser, "user");
        this.wallet = Objects.requireNonNull(newWallet, "wallet");
    }

    public void finish() {
        this.user = null;
        this.wallet = null;
    }

    public boolean isActive() {
        return user != null;
    }

    public Optional<User> user() {
        return Optional.ofNullable(user);
    }

    public Optional<Wallet> currentWallet() {
        return Optional.ofNullable(wallet);
    }

    public Wallet wallet() {
        return currentWallet().orElseThrow(this::notLoggedIn);
    }

    public CommandException notLoggedIn() {
        return new CommandException(
                "Сначала войдите: login <логин> <пароль> или зарегистрируйтесь: register <логин> <пароль>");
    }
}
