package ru.mephi.finance.cli;

public final class CommandException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CommandException(String message) {
        super(message);
    }

    public static CommandException usage(Command command) {
        return new CommandException("Неверный формат команды. Использование: " + command.usage()
                + System.lineSeparator() + "Подробнее: help " + command.name());
    }
}
