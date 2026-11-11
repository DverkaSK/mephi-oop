package ru.mephi.finance.cli;

import ru.mephi.finance.domain.error.FinanceException;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public final class FinanceCli {

    private static final System.Logger LOGGER = System.getLogger(FinanceCli.class.getName());

    private final CommandRegistry registry;
    private final CliContext context;

    public FinanceCli(CommandRegistry registry, CliContext context) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.context = Objects.requireNonNull(context, "context");
    }

    public void run(BufferedReader in) throws IOException {
        context.print(List.of(
                "Учёт личных финансов. Введите help, чтобы увидеть список команд, exit для выхода.",
                "Начните с register <логин> <пароль> или login <логин> <пароль>"));
        CommandResult result = CommandResult.CONTINUE;
        while (result == CommandResult.CONTINUE) {
            context.out().print(prompt());
            context.out().flush();
            String line = in.readLine();
            if (line == null) {
                context.out().println();
                break;
            }
            result = handle(line);
        }
        shutdown();
    }

    public CommandResult handle(String line) {
        try {
            List<String> tokens = Tokenizer.tokenize(line);
            if (tokens.isEmpty()) {
                return CommandResult.CONTINUE;
            }
            String name = tokens.getFirst();
            Command command = registry.find(name).orElseThrow(() -> new CommandException(
                    "Неизвестная команда «%s». Введите help, чтобы увидеть список команд".formatted(name)));
            if (command.requiresLogin() && !context.session().isActive()) {
                throw context.session().notLoggedIn();
            }
            Arguments args = Arguments.parse(tokens.subList(1, tokens.size()), command.options());
            return command.execute(args, context);
        } catch (CommandException | FinanceException e) {
            context.print("Ошибка: " + e.getMessage());
        } catch (RuntimeException e) {
            LOGGER.log(System.Logger.Level.ERROR, "Command failed: " + line, e);
            context.print("Непредвиденная ошибка: " + e.getMessage());
        }
        return CommandResult.CONTINUE;
    }

    public void shutdown() {
        context.session().currentWallet().ifPresent(wallet -> {
            try {
                context.print("Кошелёк сохранён в " + context.wallets().save(wallet));
            } catch (FinanceException e) {
                context.print("Ошибка сохранения: " + e.getMessage());
            }
        });
        context.session().finish();
        context.print("До свидания!");
    }

    private String prompt() {
        return context.session().user().map(user -> user.login() + "> ").orElse("> ");
    }
}
