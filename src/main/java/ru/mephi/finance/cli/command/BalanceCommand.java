package ru.mephi.finance.cli.command;

import ru.mephi.finance.cli.Arguments;
import ru.mephi.finance.cli.CliContext;
import ru.mephi.finance.cli.Command;
import ru.mephi.finance.cli.CommandResult;
import ru.mephi.finance.domain.OperationType;
import ru.mephi.finance.domain.Wallet;

import java.util.List;

public final class BalanceCommand implements Command {

    @Override
    public String name() {
        return "balance";
    }

    @Override
    public List<String> aliases() {
        return List.of("wallet");
    }

    @Override
    public String usage() {
        return "balance";
    }

    @Override
    public String description() {
        return "Текущий баланс, общие доходы и расходы";
    }

    @Override
    public CommandResult execute(Arguments args, CliContext context) {
        args.requireCount(this, 0, 0);
        Wallet wallet = context.session().wallet();
        context.print(List.of(
                "Кошелёк: " + wallet.owner(),
                "Общий доход: " + context.view().money(wallet.total(OperationType.INCOME)),
                "Общие расходы: " + context.view().money(wallet.total(OperationType.EXPENSE)),
                "Баланс: " + context.view().money(wallet.balance()),
                "Операций: " + wallet.operations().size()));
        context.print(context.view().alerts(context.alerts().overview(wallet)));
        return CommandResult.CONTINUE;
    }
}
