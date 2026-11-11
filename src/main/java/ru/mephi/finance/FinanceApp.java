package ru.mephi.finance;

import ru.mephi.finance.cli.CliContext;
import ru.mephi.finance.cli.CommandRegistry;
import ru.mephi.finance.cli.FinanceCli;
import ru.mephi.finance.cli.Session;
import ru.mephi.finance.cli.view.ConsoleView;
import ru.mephi.finance.infra.codec.CsvWalletCodec;
import ru.mephi.finance.infra.codec.JsonWalletCodec;
import ru.mephi.finance.infra.security.Pbkdf2PasswordHasher;
import ru.mephi.finance.infra.storage.FileUserRepository;
import ru.mephi.finance.infra.storage.FileWalletRepository;
import ru.mephi.finance.service.AlertService;
import ru.mephi.finance.service.AuthService;
import ru.mephi.finance.service.CategoryService;
import ru.mephi.finance.service.OperationService;
import ru.mephi.finance.service.PortabilityService;
import ru.mephi.finance.service.StatisticsService;
import ru.mephi.finance.service.TransferService;
import ru.mephi.finance.service.WalletService;
import ru.mephi.finance.service.port.UserRepository;
import ru.mephi.finance.service.port.WalletRepository;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class FinanceApp {

    private static final String DEFAULT_DATA_DIR = "data";

    private FinanceApp() {
    }

    public static void main(String[] args) throws IOException {
        Path dataDir = Path.of(Arrays.stream(args).findFirst().orElse(DEFAULT_DATA_DIR));
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        Charset inputCharset = Optional.ofNullable(System.console())
                .map(Console::charset)
                .orElse(StandardCharsets.UTF_8);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, inputCharset));
        create(dataDir, Clock.systemDefaultZone(), out).run(in);
    }

    public static FinanceCli create(Path dataDir, Clock clock, PrintStream out) {
        JsonWalletCodec json = new JsonWalletCodec();
        UserRepository users = new FileUserRepository(dataDir.resolve("users.json"));
        WalletRepository wallets = new FileWalletRepository(dataDir.resolve("wallets"), json);
        AlertService alerts = new AlertService();
        OperationService operations = new OperationService(alerts, clock);
        CliContext context = new CliContext(
                new AuthService(users, new Pbkdf2PasswordHasher()),
                new WalletService(wallets),
                operations,
                new TransferService(users, wallets, operations),
                new CategoryService(alerts),
                new StatisticsService(),
                alerts,
                new PortabilityService(List.of(json, new CsvWalletCodec())),
                new Session(),
                new ConsoleView(),
                out);
        return new FinanceCli(CommandRegistry.standard(), context);
    }
}
