package ru.mephi.finance.service;

import java.util.Objects;

public record Alert(AlertLevel level, String message) {

    public Alert {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(message, "message");
    }

    public static Alert warning(String message) {
        return new Alert(AlertLevel.WARNING, message);
    }

    public static Alert critical(String message) {
        return new Alert(AlertLevel.CRITICAL, message);
    }
}
