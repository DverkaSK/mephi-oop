package ru.mephi.finance.cli.command;

import ru.mephi.finance.cli.Arguments;
import ru.mephi.finance.cli.CliContext;
import ru.mephi.finance.cli.Command;
import ru.mephi.finance.cli.CommandResult;
import ru.mephi.finance.cli.InputParser;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public final class ExportCommand implements Command {

    @Override
    public String name() {
        return "export";
    }

    @Override
    public String usage() {
        return "export <файл.json|файл.csv> [--format json|csv]";
    }

    @Override
    public String description() {
        return "Выгрузить кошелёк (категории, бюджеты, операции) в JSON или CSV";
    }

    @Override
    public List<String> examples() {
        return List.of("export backup.json", "export reports/november.csv", "export wallet.txt --format csv");
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
        Path written = context.portability().export(context.session().wallet(), format, file);
        context.print("Кошелёк выгружен в " + written);
        return CommandResult.CONTINUE;
    }
}
