package ru.mephi.finance.cli.command;

import ru.mephi.finance.cli.Arguments;
import ru.mephi.finance.cli.CliContext;
import ru.mephi.finance.cli.Command;
import ru.mephi.finance.cli.CommandResult;
import ru.mephi.finance.domain.User;
import ru.mephi.finance.domain.Wallet;

import java.util.List;

public final class LoginCommand implements Command {

    @Override
    public String name() {
        return "login";
    }

    @Override
    public String usage() {
        return "login <логин> <пароль>";
    }

    @Override
    public String description() {
        return "Войти и загрузить кошелёк из файла";
    }

    @Override
    public List<String> examples() {
        return List.of("login alice secret123");
    }

    @Override
    public boolean requiresLogin() {
        return false;
    }

    @Override
    public CommandResult execute(Arguments args, CliContext context) {
        args.requireCount(this, 2, 2);
        User user = context.auth().authenticate(args.get(0), args.get(1));
        context.session().currentWallet().ifPresent(current -> {
            context.wallets().save(current);
            context.print("Кошелёк пользователя %s сохранён".formatted(current.owner()));
        });
        Wallet wallet = context.wallets().open(user.login());
        context.session().start(user, wallet);
        context.print("Вы вошли как %s. Операций: %d, баланс: %s".formatted(
                user.login(), wallet.operations().size(), context.view().money(wallet.balance())));
        context.print(context.view().alerts(context.alerts().overview(wallet)));
        return CommandResult.CONTINUE;
    }
}
