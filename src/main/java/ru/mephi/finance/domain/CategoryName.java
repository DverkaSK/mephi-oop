package ru.mephi.finance.domain;

import ru.mephi.finance.domain.error.ValidationException;

public final class CategoryName {

    public static final int MAX_LENGTH = 40;

    private CategoryName() {
    }

    public static String validate(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("Название категории не может быть пустым");
        }
        String name = raw.strip().replaceAll("\s+", " ");
        if (name.length() > MAX_LENGTH) {
            throw new ValidationException("Название категории длиннее %d символов".formatted(MAX_LENGTH));
        }
        if (name.startsWith("-")) {
            throw new ValidationException("Название категории не может начинаться с '-'");
        }
        if (name.chars().anyMatch(Character::isISOControl)) {
            throw new ValidationException("Название категории содержит недопустимые символы");
        }
        return name;
    }
}
