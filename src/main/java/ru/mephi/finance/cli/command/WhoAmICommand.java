package ru.mephi.finance.cli.command;

import ru.mephi.finance.cli.Arguments;
import ru.mephi.finance.cli.CliContext;
import ru.mephi.finance.cli.Command;
import ru.mephi.finance.cli.CommandResult;
import ru.mephi.finance.domain.User;

public final class WhoAmICommand implements Command {

    @Override
    public String name() {
        return "whoami";
    }

    @Override
    public String usage() {
        return "whoami";
    }

    @Override
    public String description() {
        return "Показать текущего пользователя";
    }

    @Override
    public boolean requiresLogin() {
        return false;
    }

    @Override
    public CommandResult execute(Arguments args, CliContext context) {
        args.requireCount(this, 0, 0);
        context.print(context.session().user()
                .map(User::login)
                .map(login -> "Вы вошли как " + login)
                .orElse("Вы не вошли в систему. Команды: login, register"));
        return CommandResult.CONTINUE;
    }
}
