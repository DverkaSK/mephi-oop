package ru.mephi.finance.cli.command;

import ru.mephi.finance.cli.Arguments;
import ru.mephi.finance.cli.CliContext;
import ru.mephi.finance.cli.Command;
import ru.mephi.finance.cli.CommandResult;
import ru.mephi.finance.domain.User;

import java.util.List;

public final class RegisterCommand implements Command {

    @Override
    public String name() {
        return "register";
    }

    @Override
    public String usage() {
        return "register <логин> <пароль>";
    }

    @Override
    public String description() {
        return "Зарегистрировать нового пользователя";
    }

    @Override
    public List<String> examples() {
        return List.of("register Игорь 123123");
    }

    @Override
    public boolean requiresLogin() {
        return false;
    }

    @Override
    public CommandResult execute(Arguments args, CliContext context) {
        args.requireCount(this, 2, 2);
        User user = context.auth().register(args.get(0), args.get(1));
        context.print("Пользователь %s зарегистрирован. Войдите: login %s <пароль>"
                .formatted(user.login(), user.login()));
        return CommandResult.CONTINUE;
    }
}
