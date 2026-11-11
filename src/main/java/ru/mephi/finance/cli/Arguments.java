package ru.mephi.finance.cli;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class Arguments {

    private static final String PREFIX = "--";

    private final List<String> positional;
    private final Map<String, String> options;

    private Arguments(List<String> positional, Map<String, String> options) {
        this.positional = List.copyOf(positional);
        this.options = Map.copyOf(options);
    }

    public static Arguments parse(List<String> tokens, Set<String> allowed) {
        List<String> positional = new ArrayList<>();
        Map<String, String> options = new LinkedHashMap<>();
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (!token.startsWith(PREFIX)) {
                positional.add(token);
                continue;
            }
            String name = token.substring(PREFIX.length());
            if (!allowed.contains(name)) {
                throw new CommandException(allowed.isEmpty()
                        ? "Команда не поддерживает опции, указано " + token
                        : "Неизвестная опция %s, доступны: %s".formatted(token, allowed.stream()
                                .sorted()
                                .map(option -> PREFIX + option)
                                .collect(Collectors.joining(", "))));
            }
            if (i + 1 >= tokens.size() || tokens.get(i + 1).startsWith(PREFIX)) {
                throw new CommandException("Для опции %s не указано значение".formatted(token));
            }
            if (options.put(name, tokens.get(++i)) != null) {
                throw new CommandException("Опция %s указана несколько раз".formatted(token));
            }
        }
        return new Arguments(positional, options);
    }

    public List<String> positional() {
        return positional;
    }

    public int size() {
        return positional.size();
    }

    public boolean isEmpty() {
        return positional.isEmpty();
    }

    public String get(int index) {
        return positional.get(index);
    }

    public List<String> from(int index) {
        return positional.subList(Math.min(index, positional.size()), positional.size());
    }

    public Optional<String> option(String name) {
        return Optional.ofNullable(options.get(name));
    }

    public Arguments requireCount(Command command, int min, int max) {
        if (positional.size() < min || positional.size() > max) {
            throw CommandException.usage(command);
        }
        return this;
    }
}
