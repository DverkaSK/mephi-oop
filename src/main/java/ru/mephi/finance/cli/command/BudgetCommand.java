package ru.mephi.finance.cli.command;

import ru.mephi.finance.cli.Arguments;
import ru.mephi.finance.cli.CliContext;
import ru.mephi.finance.cli.Command;
import ru.mephi.finance.cli.CommandException;
import ru.mephi.finance.cli.CommandResult;
import ru.mephi.finance.cli.InputParser;
import ru.mephi.finance.domain.Wallet;
import ru.mephi.finance.service.CategoryService;
import ru.mephi.finance.service.OperationFilter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

public final class BudgetCommand implements Command {

    @Override
    public String name() {
        return "budget";
    }

    @Override
    public List<String> aliases() {
        return List.of("budgets");
    }

    @Override
    public String usage() {
        return "budget [list | set <категория> <сумма> | delete <категория>]";
    }

    @Override
    public String description() {
        return "Бюджеты по категориям: просмотр с остатком, установка и изменение лимита, удаление";
    }

    @Override
    public List<String> examples() {
        return List.of(
                "budget",
                "budget set Еда 4000",
                "budget set \"Коммунальные услуги\" 2500",
                "budget delete Развлечения");
    }

    @Override
    public CommandResult execute(Arguments args, CliContext context) {
        Wallet wallet = context.session().wallet();
        String action = args.isEmpty() ? "list" : args.get(0).toLowerCase(Locale.ROOT);
        switch (action) {
            case "list" -> {
                args.requireCount(this, 0, 1);
                context.print("Бюджет по категориям:");
                context.print(context.view().budgets(
                        context.statistics().summarize(wallet, OperationFilter.all()).budgets()));
            }
            case "set" -> {
                args.requireCount(this, 3, Integer.MAX_VALUE);
                String category = String.join(" ", args.positional().subList(1, args.size() - 1));
                BigDecimal limit = InputParser.amount(args.get(args.size() - 1));
                CategoryService.BudgetChange change = context.categories().setBudget(wallet, category, limit);
                context.print(change.previous()
                        .map(previous -> "Бюджет «%s» изменён: %s -> %s".formatted(change.category(),
                                context.view().money(previous), context.view().money(change.limit())))
                        .orElse("Бюджет «%s» установлен: %s".formatted(
                                change.category(), context.view().money(change.limit()))));
                context.print(context.view().alerts(change.alerts()));
            }
            case "delete", "remove" -> {
                args.requireCount(this, 2, Integer.MAX_VALUE);
                String category = String.join(" ", args.from(1));
                BigDecimal removed = context.categories().removeBudget(wallet, category);
                context.print("Бюджет %s для категории «%s» удалён"
                        .formatted(context.view().money(removed), wallet.requireCategory(category)));
            }
            default -> throw new CommandException("Неизвестное действие «%s». Использование: %s"
                    .formatted(action, usage()));
        }
        return CommandResult.CONTINUE;
    }
}
