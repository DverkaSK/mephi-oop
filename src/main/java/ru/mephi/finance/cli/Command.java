package ru.mephi.finance.cli;

import java.util.List;
import java.util.Set;

public interface Command {

    String name();

    default List<String> aliases() {
        return List.of();
    }

    String usage();

    String description();

    default List<String> examples() {
        return List.of();
    }

    default Set<String> options() {
        return Set.of();
    }

    default boolean requiresLogin() {
        return true;
    }

    CommandResult execute(Arguments args, CliContext context);
}
