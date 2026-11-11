package ru.mephi.finance.cli;

import ru.mephi.finance.cli.command.AddOperationCommand;
import ru.mephi.finance.cli.command.AlertsCommand;
import ru.mephi.finance.cli.command.BalanceCommand;
import ru.mephi.finance.cli.command.BudgetCommand;
import ru.mephi.finance.cli.command.CategoryCommand;
import ru.mephi.finance.cli.command.ClearCommand;
import ru.mephi.finance.cli.command.ExitCommand;
import ru.mephi.finance.cli.command.ExportCommand;
import ru.mephi.finance.cli.command.HelpCommand;
import ru.mephi.finance.cli.command.HistoryCommand;
import ru.mephi.finance.cli.command.ImportCommand;
import ru.mephi.finance.cli.command.LoginCommand;
import ru.mephi.finance.cli.command.LogoutCommand;
import ru.mephi.finance.cli.command.RegisterCommand;
import ru.mephi.finance.cli.command.SaveCommand;
import ru.mephi.finance.cli.command.StatsCommand;
import ru.mephi.finance.cli.command.TransferCommand;
import ru.mephi.finance.cli.command.WhoAmICommand;
import ru.mephi.finance.domain.OperationType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public final class CommandRegistry {

    private final List<Command> commands = new ArrayList<>();
    private final Map<String, Command> lookup = new HashMap<>();

    public static CommandRegistry standard() {
        CommandRegistry registry = new CommandRegistry();
        return registry
                .register(new HelpCommand(registry))
                .register(new RegisterCommand())
                .register(new LoginCommand())
                .register(new WhoAmICommand())
                .register(new AddOperationCommand(OperationType.INCOME))
                .register(new AddOperationCommand(OperationType.EXPENSE))
                .register(new TransferCommand())
                .register(new BalanceCommand())
                .register(new HistoryCommand())
                .register(new StatsCommand())
                .register(new CategoryCommand())
                .register(new BudgetCommand())
                .register(new AlertsCommand())
                .register(new ExportCommand())
                .register(new ImportCommand())
                .register(new SaveCommand())
                .register(new ClearCommand())
                .register(new LogoutCommand())
                .register(new ExitCommand());
    }

    public CommandRegistry register(Command command) {
        List<String> keys = Stream.concat(Stream.of(command.name()), command.aliases().stream())
                .map(this::normalize)
                .toList();
        keys.stream()
                .filter(lookup::containsKey)
                .findFirst()
                .ifPresent(key -> {
                    throw new IllegalArgumentException("Команда уже зарегистрирована: " + key);
                });
        keys.forEach(key -> lookup.put(key, command));
        commands.add(command);
        return this;
    }

    public Optional<Command> find(String name) {
        return Optional.ofNullable(lookup.get(normalize(name)));
    }

    public List<Command> commands() {
        return List.copyOf(commands);
    }

    private String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
