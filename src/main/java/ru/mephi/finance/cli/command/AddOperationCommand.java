package ru.mephi.finance.cli.command;

import ru.mephi.finance.cli.Arguments;
import ru.mephi.finance.cli.CliContext;
import ru.mephi.finance.cli.Command;
import ru.mephi.finance.cli.CommandResult;
import ru.mephi.finance.cli.InputParser;
import ru.mephi.finance.domain.OperationType;
import ru.mephi.finance.service.OperationResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class AddOperationCommand implements Command {

    private final OperationType type;

    public AddOperationCommand(OperationType type) {
        this.type = Objects.requireNonNull(type, "type");
    }

    @Override
    public String name() {
        return type == OperationType.INCOME ? "income" : "expense";
    }

    @Override
    public List<String> aliases() {
        return type == OperationType.INCOME ? List.of("in") : List.of("out");
    }

    @Override
    public String usage() {
        return name() + " <категория> <сумма> [--date ГГГГ-ММ-ДД] [--note текст]";
    }

    @Override
    public String description() {
        return type == OperationType.INCOME
                ? "Добавить доход в категорию (новая категория создаётся автоматически)"
                : "Добавить расход в категорию (новая категория создаётся автоматически)";
    }

    @Override
    public List<String> examples() {
        return type == OperationType.INCOME
                ? List.of("income Зарплата 40000", "income Бонус 3000 --date 2026-11-01 --note \"премия за квартал\"")
                : List.of(
                        "expense Еда 300",
                        "expense \"Коммунальные услуги\" 3000",
                        "expense Такси 1500 --note аэропорт");
    }

    @Override
    public Set<String> options() {
        return Set.of("date", "note");
    }

    @Override
    public CommandResult execute(Arguments args, CliContext context) {
        args.requireCount(this, 2, Integer.MAX_VALUE);
        String category = String.join(" ", args.positional().subList(0, args.size() - 1));
        BigDecimal amount = InputParser.amount(args.get(args.size() - 1));
        LocalDate date = args.option("date").map(InputParser::date).orElse(null);
        String note = args.option("note").orElse("");
        OperationResult result = context.operations()
                .add(context.session().wallet(), type, category, amount, date, note);
        context.print(context.view().operationResult(result));
        return CommandResult.CONTINUE;
    }
}
