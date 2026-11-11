package ru.mephi.finance.cli.command;

import ru.mephi.finance.cli.Arguments;
import ru.mephi.finance.cli.CliContext;
import ru.mephi.finance.cli.Command;
import ru.mephi.finance.cli.CommandResult;
import ru.mephi.finance.cli.InputParser;
import ru.mephi.finance.service.OperationResult;

import java.util.List;
import java.util.Set;

public final class TransferCommand implements Command {

    @Override
    public String name() {
        return "transfer";
    }

    @Override
    public String usage() {
        return "transfer <логин получателя> <сумма> [--note текст]";
    }

    @Override
    public String description() {
        return "Перевести деньги другому пользователю (расход у вас, доход у получателя)";
    }

    @Override
    public List<String> examples() {
        return List.of("transfer bob 1500", "transfer bob 500 --note \"за обед\"");
    }

    @Override
    public Set<String> options() {
        return Set.of("note");
    }

    @Override
    public CommandResult execute(Arguments args, CliContext context) {
        args.requireCount(this, 2, 2);
        String recipient = args.get(0);
        OperationResult result = context.transfers().transfer(
                context.session().wallet(), recipient, InputParser.amount(args.get(1)), args.option("note").orElse(""));
        context.print("Перевод %s пользователю %s выполнен. Баланс: %s".formatted(
                context.view().money(result.operation().amount()), recipient, context.view().money(result.balance())));
        context.print(context.view().alerts(result.alerts()));
        return CommandResult.CONTINUE;
    }
}
