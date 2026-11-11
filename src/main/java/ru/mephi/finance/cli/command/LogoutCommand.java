package ru.mephi.finance.cli.command;

import ru.mephi.finance.cli.Arguments;
import ru.mephi.finance.cli.CliContext;
import ru.mephi.finance.cli.Command;
import ru.mephi.finance.cli.CommandResult;
import ru.mephi.finance.domain.Wallet;

import java.nio.file.Path;

public final class LogoutCommand implements Command {

    @Override
    public String name() {
        return "logout";
    }

    @Override
    public String usage() {
        return "logout";
    }

    @Override
    public String description() {
        return "Сохранить кошелёк в файл и выйти из аккаунта";
    }

    @Override
    public CommandResult execute(Arguments args, CliContext context) {
        args.requireCount(this, 0, 0);
        Wallet wallet = context.session().wallet();
        Path file = context.wallets().save(wallet);
        context.session().finish();
        context.print("Кошелёк сохранён в %s. Вы вышли из аккаунта %s".formatted(file, wallet.owner()));
        return CommandResult.CONTINUE;
    }
}
