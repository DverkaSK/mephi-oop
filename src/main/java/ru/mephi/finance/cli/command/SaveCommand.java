package ru.mephi.finance.cli.command;

import ru.mephi.finance.cli.Arguments;
import ru.mephi.finance.cli.CliContext;
import ru.mephi.finance.cli.Command;
import ru.mephi.finance.cli.CommandResult;

public final class SaveCommand implements Command {

    @Override
    public String name() {
        return "save";
    }

    @Override
    public String usage() {
        return "save";
    }

    @Override
    public String description() {
        return "Сохранить кошелёк в файл, не выходя из приложения";
    }

    @Override
    public CommandResult execute(Arguments args, CliContext context) {
        args.requireCount(this, 0, 0);
        context.print("Кошелёк сохранён в " + context.wallets().save(context.session().wallet()));
        return CommandResult.CONTINUE;
    }
}
