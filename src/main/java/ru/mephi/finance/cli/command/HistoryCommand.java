package ru.mephi.finance.cli.command;

import ru.mephi.finance.cli.Arguments;
import ru.mephi.finance.cli.CliContext;
import ru.mephi.finance.cli.Command;
import ru.mephi.finance.cli.CommandResult;
import ru.mephi.finance.cli.InputParser;
import ru.mephi.finance.domain.Operation;
import ru.mephi.finance.service.StatisticsService;

import java.util.List;
import java.util.Set;

public final class HistoryCommand implements Command {

    private static final int DEFAULT_LIMIT = 20;

    @Override
    public String name() {
        return "history";
    }

    @Override
    public List<String> aliases() {
        return List.of("ops");
    }

    @Override
    public String usage() {
        return "history [категории...] [--from ГГГГ-ММ-ДД] [--to ГГГГ-ММ-ДД] [--limit N]";
    }

    @Override
    public String description() {
        return "История операций с фильтром по категориям и периоду (по умолчанию последние "
                + DEFAULT_LIMIT + ")";
    }

    @Override
    public List<String> examples() {
        return List.of("history", "history Еда Такси", "history --from 2026-11-01 --to 2026-11-30 --limit 50");
    }

    @Override
    public Set<String> options() {
        return Set.of("from", "to", "limit");
    }

    @Override
    public CommandResult execute(Arguments args, CliContext context) {
        int limit = args.option("limit").map(value -> InputParser.positiveInt(value, "Лимит")).orElse(DEFAULT_LIMIT);
        StatisticsService.Selection selection = context.statistics()
                .select(context.session().wallet(), InputParser.filter(args, args.positional()));
        selection.missing().forEach(name -> context.print("[!] Категория «%s» не найдена".formatted(name)));
        List<Operation> operations = selection.operations();
        List<Operation> tail = operations.subList(Math.max(0, operations.size() - limit), operations.size());
        context.print(context.view().operations(tail));
        if (tail.size() < operations.size()) {
            context.print("Показано %d из %d операций, увеличьте --limit чтобы увидеть больше"
                    .formatted(tail.size(), operations.size()));
        }
        return CommandResult.CONTINUE;
    }
}
