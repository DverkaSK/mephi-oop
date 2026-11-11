package ru.mephi.finance.cli.command;

import ru.mephi.finance.cli.Arguments;
import ru.mephi.finance.cli.CliContext;
import ru.mephi.finance.cli.Command;
import ru.mephi.finance.cli.CommandResult;
import ru.mephi.finance.cli.InputParser;
import ru.mephi.finance.domain.Wallet;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public final class ImportCommand implements Command {

    @Override
    public String name() {
        return "import";
    }

    @Override
    public String usage() {
        return "import <файл.json|файл.csv> [--format json|csv]";
    }

    @Override
    public String description() {
        return "Загрузить данные из JSON или CSV; уже существующие операции пропускаются";
    }

    @Override
    public List<String> examples() {
        return List.of("import backup.json", "import reports/november.csv");
    }

    @Override
    public Set<String> options() {
        return Set.of("format");
    }

    @Override
    public CommandResult execute(Arguments args, CliContext context) {
        args.requireCount(this, 1, 1);
        Path file = InputParser.path(args.get(0));
        String format = args.option("format").orElseGet(() -> context.portability().detectFormat(file));
        Wallet wallet = context.session().wallet();
        Wallet.MergeResult result = context.portability().importInto(wallet, format, file);
        context.print("Импорт завершён: операций добавлено %d, пропущено дубликатов %d, новых категорий %d, бюджетов %d"
                .formatted(result.addedOperations(), result.skippedOperations(), result.addedCategories(),
                        result.budgets()));
        context.print(context.view().alerts(context.alerts().overview(wallet)));
        return CommandResult.CONTINUE;
    }
}
