package ru.mephi.finance.cli.command;

import ru.mephi.finance.cli.Arguments;
import ru.mephi.finance.cli.CliContext;
import ru.mephi.finance.cli.Command;
import ru.mephi.finance.cli.CommandException;
import ru.mephi.finance.cli.CommandResult;
import ru.mephi.finance.cli.InputParser;
import ru.mephi.finance.service.Summary;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class StatsCommand implements Command {

    @Override
    public String name() {
        return "stats";
    }

    @Override
    public List<String> aliases() {
        return List.of("report");
    }

    @Override
    public String usage() {
        return "stats [категории...] [--from ГГГГ-ММ-ДД] [--to ГГГГ-ММ-ДД] [--file путь]";
    }

    @Override
    public String description() {
        return "Доходы и расходы по категориям, состояние бюджетов; вывод в терминал или в файл";
    }

    @Override
    public List<String> examples() {
        return List.of(
                "stats",
                "stats Еда Такси \"Коммунальные услуги\"",
                "stats --from 2026-11-01 --to 2026-11-30",
                "stats --file report.txt");
    }

    @Override
    public Set<String> options() {
        return Set.of("from", "to", "file");
    }

    @Override
    public CommandResult execute(Arguments args, CliContext context) {
        Summary summary = context.statistics()
                .summarize(context.session().wallet(), InputParser.filter(args, args.positional()));
        List<String> lines = context.view().summary(summary);
        Optional<Path> file = args.option("file").map(InputParser::path);
        if (file.isEmpty()) {
            context.print(lines);
            return CommandResult.CONTINUE;
        }
        Path target = file.get().toAbsolutePath();
        try {
            Optional.ofNullable(target.getParent()).ifPresent(this::createDirectories);
            Files.write(target, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CommandException("Не удалось записать отчёт в %s: %s".formatted(target, e.getMessage()));
        }
        context.print("Отчёт сохранён в " + target);
        return CommandResult.CONTINUE;
    }

    private void createDirectories(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new CommandException("Не удалось создать папку " + directory);
        }
    }
}
