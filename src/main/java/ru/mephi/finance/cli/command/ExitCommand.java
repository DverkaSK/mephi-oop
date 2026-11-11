package ru.mephi.finance.cli.command;

import ru.mephi.finance.cli.Arguments;
import ru.mephi.finance.cli.CliContext;
import ru.mephi.finance.cli.Command;
import ru.mephi.finance.cli.CommandResult;

import java.util.List;

public final class ExitCommand implements Command {

    @Override
    public String name() {
        return "exit";
    }

    @Override
    public List<String> aliases() {
        return List.of("quit");
    }

    @Override
    public String usage() {
        return "exit";
    }

    @Override
    public String description() {
        return "Сохранить кошелёк и выйти из приложения";
    }

    @Override
    public boolean requiresLogin() {
        return false;
    }

    @Override
    public CommandResult execute(Arguments args, CliContext context) {
        args.requireCount(this, 0, 0);
        return CommandResult.EXIT;
    }
}
