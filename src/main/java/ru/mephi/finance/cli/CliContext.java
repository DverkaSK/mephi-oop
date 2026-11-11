package ru.mephi.finance.cli;

import ru.mephi.finance.cli.view.ConsoleView;
import ru.mephi.finance.service.AlertService;
import ru.mephi.finance.service.AuthService;
import ru.mephi.finance.service.CategoryService;
import ru.mephi.finance.service.OperationService;
import ru.mephi.finance.service.PortabilityService;
import ru.mephi.finance.service.StatisticsService;
import ru.mephi.finance.service.TransferService;
import ru.mephi.finance.service.WalletService;

import java.io.PrintStream;
import java.util.List;
import java.util.Objects;

public record CliContext(
        AuthService auth,
        WalletService wallets,
        OperationService operations,
        TransferService transfers,
        CategoryService categories,
        StatisticsService statistics,
        AlertService alerts,
        PortabilityService portability,
        Session session,
        ConsoleView view,
        PrintStream out) {

    public CliContext {
        List.of(auth, wallets, operations, transfers, categories, statistics, alerts, portability, session, view, out)
                .forEach(Objects::requireNonNull);
    }

    public void print(List<String> lines) {
        lines.forEach(out::println);
    }

    public void print(String line) {
        out.println(line);
    }
}
