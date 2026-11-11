package ru.mephi.finance.cli;

import ru.mephi.finance.service.OperationFilter;

import java.math.BigDecimal;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Pattern;

public final class InputParser {

    private static final Pattern AMOUNT = Pattern.compile("\\d{1,13}([.,]\\d{1,2})?");

    private InputParser() {
    }

    public static BigDecimal amount(String raw) {
        String value = raw.strip();
        if (value.startsWith("-")) {
            throw new CommandException("Сумма должна быть положительной, указано '%s'".formatted(raw));
        }
        if (!AMOUNT.matcher(value).matches()) {
            throw new CommandException(
                    "'%s' не является суммой. Укажите число, например 1500 или 99.90".formatted(raw));
        }
        return new BigDecimal(value.replace(',', '.'));
    }

    public static LocalDate date(String raw) {
        try {
            return LocalDate.parse(raw.strip());
        } catch (DateTimeParseException e) {
            throw new CommandException("Неверная дата '%s', нужен формат ГГГГ-ММ-ДД, например 2026-11-01"
                    .formatted(raw));
        }
    }

    public static int positiveInt(String raw, String what) {
        try {
            int value = Integer.parseInt(raw.strip());
            if (value <= 0) {
                throw new NumberFormatException(raw);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new CommandException("%s должно быть целым положительным числом, указано '%s'".formatted(what, raw));
        }
    }

    public static OperationFilter filter(Arguments args, List<String> categories) {
        return new OperationFilter(
                categories,
                args.option("from").map(InputParser::date),
                args.option("to").map(InputParser::date));
    }

    public static Path path(String raw) {
        if (raw.isBlank()) {
            throw new CommandException("Путь к файлу не может быть пустым");
        }
        try {
            return Path.of(raw.strip());
        } catch (InvalidPathException e) {
            throw new CommandException("Некорректный путь к файлу: " + raw);
        }
    }
}
