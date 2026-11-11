package ru.mephi.finance.cli.view;

import ru.mephi.finance.domain.Operation;
import ru.mephi.finance.domain.Wallet;
import ru.mephi.finance.service.Alert;
import ru.mephi.finance.service.AlertLevel;
import ru.mephi.finance.service.AlertService;
import ru.mephi.finance.service.OperationFilter;
import ru.mephi.finance.service.OperationResult;
import ru.mephi.finance.service.Summary;
import ru.mephi.finance.service.format.MoneyFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ConsoleView {

    private static final String INDENT = "  ";

    public String money(BigDecimal value) {
        return MoneyFormat.format(value);
    }

    public List<String> alerts(List<Alert> alerts) {
        return alerts.stream()
                .map(alert -> (alert.level() == AlertLevel.CRITICAL ? "[!!] " : "[!] ") + alert.message())
                .toList();
    }

    public List<String> operationResult(OperationResult result) {
        Operation operation = result.operation();
        Stream<String> created = result.categoryCreated()
                ? Stream.of("Создана новая категория «%s»".formatted(operation.category()))
                : Stream.empty();
        Stream<String> added = Stream.of("%s %s, категория «%s», дата %s. Баланс: %s".formatted(
                operation.isIncome() ? "Добавлен доход" : "Добавлен расход",
                money(operation.amount()),
                operation.category(),
                operation.date(),
                money(result.balance())));
        return Stream.of(created, added, alerts(result.alerts()).stream()).flatMap(stream -> stream).toList();
    }

    public List<String> operations(List<Operation> operations) {
        if (operations.isEmpty()) {
            return List.of("Операций не найдено");
        }
        TextTable table = TextTable.of("Дата", "Тип", "Категория", "Сумма", "Комментарий").alignRight(3);
        operations.forEach(operation -> table.row(
                operation.date(),
                operation.type().title(),
                operation.category(),
                (operation.isIncome() ? "+" : "-") + money(operation.amount()),
                operation.note()));
        return table.render();
    }

    public List<String> categories(Wallet wallet) {
        if (wallet.categories().isEmpty()) {
            return List.of("Категорий пока нет. Создайте: category add <название>");
        }
        Map<String, Long> usage = wallet.operations().stream()
                .collect(Collectors.groupingBy(Operation::category, Collectors.counting()));
        TextTable table = TextTable.of("Категория", "Операций", "Бюджет").alignRight(1, 2);
        wallet.categories().forEach(category -> table.row(
                category,
                usage.getOrDefault(category, 0L),
                wallet.budget(category).map(this::money).orElse("нет")));
        return table.render();
    }

    public List<String> budgets(List<Summary.BudgetStatus> budgets) {
        if (budgets.isEmpty()) {
            return List.of(INDENT + "Бюджеты не установлены. Установить: budget set <категория> <сумма>");
        }
        TextTable table = TextTable.of("Категория", "Бюджет", "Потрачено", "Оставшийся бюджет", "Использовано", "")
                .alignRight(1, 2, 3, 4);
        budgets.forEach(status -> table.row(
                status.category(),
                money(status.limit()),
                money(status.spent()),
                money(status.remaining()),
                percent(status),
                marker(status)));
        return table.render().stream().map(line -> INDENT + line).toList();
    }

    public List<String> summary(Summary summary) {
        Stream<String> header = Stream.of(
                "Отчёт по кошельку " + summary.owner(),
                "Период: " + period(summary.filter()) + ", категории: "
                        + (summary.categories().isEmpty() ? "все" : String.join(", ", summary.categories())),
                "Операций в выборке: " + summary.operationCount(),
                "");
        Stream<String> missing = summary.missingCategories().stream()
                .map(name -> "[!] Категория «%s» не найдена и не учтена в отчёте".formatted(name));
        Stream<String> empty = summary.operationCount() == 0
                ? Stream.of("[!] Нет операций, подходящих под условия выборки")
                : Stream.empty();
        Stream<String> body = Stream.of(
                Stream.of("Общий доход: " + money(summary.totalIncome()), "Доходы по категориям:"),
                byCategory(summary.incomeByCategory()),
                Stream.of("Общие расходы: " + money(summary.totalExpense()), "Расходы по категориям:"),
                byCategory(summary.expenseByCategory()),
                Stream.of("Бюджет по категориям:"),
                budgets(summary.budgets()).stream(),
                Stream.of("", "Баланс: " + money(summary.balance())))
                .flatMap(stream -> stream);
        return Stream.of(header, missing, empty, body).flatMap(stream -> stream).toList();
    }

    private Stream<String> byCategory(Map<String, BigDecimal> values) {
        if (values.isEmpty()) {
            return Stream.of(INDENT + "нет данных");
        }
        TextTable table = TextTable.of("Категория", "Сумма").alignRight(1);
        values.forEach((category, amount) -> table.row(category, money(amount)));
        return table.render().stream().skip(2).map(line -> INDENT + line);
    }

    private String period(OperationFilter filter) {
        if (!filter.hasPeriod()) {
            return "всё время";
        }
        return "с " + filter.from().map(LocalDate::toString).orElse("начала") + " по "
                + filter.to().map(LocalDate::toString).orElse("сегодня");
    }

    private String percent(Summary.BudgetStatus status) {
        int value = status.percentUsed();
        return value == Integer.MAX_VALUE ? "-" : value + "%";
    }

    private String marker(Summary.BudgetStatus status) {
        if (status.exceeded()) {
            return "превышен";
        }
        if (status.remaining().signum() == 0) {
            return "исчерпан";
        }
        return status.percentUsed() >= AlertService.WARNING_PERCENT ? "почти исчерпан" : "";
    }
}
