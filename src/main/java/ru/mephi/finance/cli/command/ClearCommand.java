package ru.mephi.finance.cli.command;

import ru.mephi.finance.cli.Arguments;
import ru.mephi.finance.cli.CliContext;
import ru.mephi.finance.cli.Command;
import ru.mephi.finance.cli.CommandException;
import ru.mephi.finance.cli.CommandResult;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public final class ClearCommand implements Command {

    private static final String ANSI_CLEAR = "\u001B[H\u001B[2J\u001B[3J";

    @Override
    public String name() {
        return "clear";
    }

    @Override
    public List<String> aliases() {
        return List.of("cls");
    }

    @Override
    public String usage() {
        return "clear";
    }

    @Override
    public String description() {
        return "Очистить экран";
    }

    @Override
    public boolean requiresLogin() {
        return false;
    }

    @Override
    public CommandResult execute(Arguments args, CliContext context) {
        args.requireCount(this, 0, 0);
        if (System.console() == null) {
            return CommandResult.CONTINUE;
        }
        if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("windows")) {
            clearWindowsConsole();
        } else {
            context.out().print(ANSI_CLEAR);
            context.out().flush();
        }
        return CommandResult.CONTINUE;
    }

    private void clearWindowsConsole() {
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (IOException e) {
            throw new CommandException("Не удалось очистить экран: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommandException("Очистка экрана прервана");
        }
    }
}
