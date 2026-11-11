package ru.mephi.finance.cli.command;

import ru.mephi.finance.cli.Arguments;
import ru.mephi.finance.cli.CliContext;
import ru.mephi.finance.cli.Command;
import ru.mephi.finance.cli.CommandException;
import ru.mephi.finance.cli.CommandRegistry;
import ru.mephi.finance.cli.CommandResult;
import ru.mephi.finance.cli.view.TextTable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class HelpCommand implements Command {

    private final CommandRegistry registry;

    public HelpCommand(CommandRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    @Override
    public String name() {
        return "help";
    }

    @Override
    public List<String> aliases() {
        return List.of("?");
    }

    @Override
    public String usage() {
        return "help [команда]";
    }

    @Override
    public String description() {
        return "Список команд или подробная справка по команде";
    }

    @Override
    public List<String> examples() {
        return List.of("help", "help budget");
    }

    @Override
    public boolean requiresLogin() {
        return false;
    }

    @Override
    public CommandResult execute(Arguments args, CliContext context) {
        args.requireCount(this, 0, 1);
        if (args.isEmpty()) {
            TextTable table = TextTable.of("Команда", "Описание");
            registry.commands().forEach(command -> table.row(title(command), command.description()));
            context.print(table.render());
            context.print(List.of(
                    "Синтаксис и примеры команды: help <команда>, например help expense",
                    "Аргументы с пробелами пишите в кавычках: \"Коммунальные услуги\""));
            return CommandResult.CONTINUE;
        }
        Command command = registry.find(args.get(0))
                .orElseThrow(() -> new CommandException("Команда «%s» не найдена".formatted(args.get(0))));
        Stream<String> aliases = command.aliases().isEmpty()
                ? Stream.empty()
                : Stream.of("Синонимы: " + String.join(", ", command.aliases()));
        Stream<String> login = command.requiresLogin() ? Stream.of("Требует входа в систему") : Stream.empty();
        Stream<String> examples = command.examples().isEmpty()
                ? Stream.empty()
                : Stream.concat(Stream.of("Примеры:"), command.examples().stream().map(example -> "  " + example));
        context.print(Stream.of(
                        Stream.of(command.usage(), "  " + command.description()), aliases, login, examples)
                .flatMap(stream -> stream)
                .toList());
        return CommandResult.CONTINUE;
    }

    private String title(Command command) {
        return command.aliases().isEmpty()
                ? command.name()
                : command.name() + " (" + String.join(", ", command.aliases()) + ")";
    }
}
