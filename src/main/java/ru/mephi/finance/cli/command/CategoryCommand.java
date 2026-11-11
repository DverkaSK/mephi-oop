package ru.mephi.finance.cli.command;

import ru.mephi.finance.cli.Arguments;
import ru.mephi.finance.cli.CliContext;
import ru.mephi.finance.cli.Command;
import ru.mephi.finance.cli.CommandException;
import ru.mephi.finance.cli.CommandResult;
import ru.mephi.finance.domain.Wallet;

import java.util.List;
import java.util.Locale;

public final class CategoryCommand implements Command {

    @Override
    public String name() {
        return "category";
    }

    @Override
    public List<String> aliases() {
        return List.of("categories");
    }

    @Override
    public String usage() {
        return "category [list | add <название> | rename <старое> <новое> | delete <название>]";
    }

    @Override
    public String description() {
        return "Просмотр, создание, переименование и удаление категорий";
    }

    @Override
    public List<String> examples() {
        return List.of(
                "category",
                "category add Подарки",
                "category rename Такси Транспорт",
                "category rename \"Коммунальные услуги\" ЖКХ",
                "category delete Подарки");
    }

    @Override
    public CommandResult execute(Arguments args, CliContext context) {
        Wallet wallet = context.session().wallet();
        String action = args.isEmpty() ? "list" : args.get(0).toLowerCase(Locale.ROOT);
        switch (action) {
            case "list" -> {
                args.requireCount(this, 0, 1);
                context.print(context.view().categories(wallet));
            }
            case "add" -> {
                args.requireCount(this, 2, Integer.MAX_VALUE);
                String created = context.categories().create(wallet, String.join(" ", args.from(1)));
                context.print("Категория «%s» создана".formatted(created));
            }
            case "rename" -> {
                args.requireCount(this, 3, 3);
                String renamed = context.categories().rename(wallet, args.get(1), args.get(2));
                context.print("Категория «%s» переименована в «%s», операции и бюджет перенесены"
                        .formatted(args.get(1), renamed));
            }
            case "delete", "remove" -> {
                args.requireCount(this, 2, Integer.MAX_VALUE);
                String removed = context.categories().delete(wallet, String.join(" ", args.from(1)));
                context.print("Категория «%s» удалена".formatted(removed));
            }
            default -> throw new CommandException("Неизвестное действие «%s». Использование: %s"
                    .formatted(action, usage()));
        }
        return CommandResult.CONTINUE;
    }
}
