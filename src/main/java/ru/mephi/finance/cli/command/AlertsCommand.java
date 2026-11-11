package ru.mephi.finance.cli.command;

import ru.mephi.finance.cli.Arguments;
import ru.mephi.finance.cli.CliContext;
import ru.mephi.finance.cli.Command;
import ru.mephi.finance.cli.CommandResult;
import ru.mephi.finance.service.Alert;

import java.util.List;

public final class AlertsCommand implements Command {

    @Override
    public String name() {
        return "alerts";
    }

    @Override
    public String usage() {
        return "alerts";
    }

    @Override
    public String description() {
        return "Показать текущие предупреждения по бюджетам и балансу";
    }

    @Override
    public CommandResult execute(Arguments args, CliContext context) {
        args.requireCount(this, 0, 0);
        List<Alert> alerts = context.alerts().overview(context.session().wallet());
        context.print(alerts.isEmpty() ? List.of("Предупреждений нет") : context.view().alerts(alerts));
        return CommandResult.CONTINUE;
    }
}
