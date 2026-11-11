package ru.mephi.finance;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.mephi.finance.support.Fixtures;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class FinanceScenarioTest {

    @Test
    public void exampleFromAssignmentProducesExpectedReport(@TempDir Path dir) throws IOException {
        String output = run(dir,
                "register alice secret123",
                "login alice secret123",
                "expense Еда 300",
                "expense Еда 500",
                "expense Развлечения 3000",
                "expense \"Коммунальные услуги\" 3000",
                "expense Такси 1500",
                "income Зарплата 20000",
                "income Зарплата 40000",
                "income Бонус 3000",
                "budget set Еда 4000",
                "budget set Развлечения 3000",
                "budget set \"Коммунальные услуги\" 2500",
                "stats",
                "exit");

        assertContains(output,
                "Общий доход: 63,000.00",
                "Зарплата   60,000.00",
                "Общие расходы: 8,300.00",
                "-500.00",
                "3,200.00",
                "[!!] Превышен бюджет «Коммунальные услуги»",
                "Кошелёк сохранён в");
        assertTrue(Files.isRegularFile(dir.resolve("data/wallets/alice.json")));
    }

    @Test
    public void walletIsRestoredAfterRestartAndTransfersReachRecipient(@TempDir Path dir) throws IOException {
        run(dir,
                "register alice secret123",
                "register bob qwerty",
                "login alice secret123",
                "income Зарплата 5000",
                "transfer bob 1200 --note обед",
                "exit");

        String output = run(dir,
                "login alice secret123",
                "balance",
                "login bob qwerty",
                "history");

        assertContains(output,
                "Вы вошли как alice. Операций: 2, баланс: 3,800.00",
                "Вы вошли как bob. Операций: 1, баланс: 1,200.00",
                "Перевод от alice: обед");
    }

    @Test
    public void reportFileExportAndImportWork(@TempDir Path dir) throws IOException {
        Path report = dir.resolve("out/report.txt");
        Path export = dir.resolve("out/wallet.csv");
        run(dir,
                "register alice secret123",
                "register carol secret123",
                "login alice secret123",
                "income Зарплата 1000",
                "expense Еда 250",
                "stats --file \"" + report + "\"",
                "export \"" + export + "\"",
                "login carol secret123",
                "import \"" + export + "\"",
                "import \"" + export + "\"",
                "exit");

        String carol = Files.readString(dir.resolve("data/wallets/carol.json"), StandardCharsets.UTF_8);
        assertContains(Files.readString(report, StandardCharsets.UTF_8), "Общие расходы: 250.00");
        assertContains(carol, "\"Зарплата\"", "\"250.00\"");
    }

    @Test
    public void invalidInputIsReportedWithoutStoppingLoop(@TempDir Path dir) throws IOException {
        String output = run(dir,
                "expense Еда 100",
                "register alice secret123",
                "login alice wrong",
                "login alice secret123",
                "expense Еда abc",
                "expense Еда 100 --date 2026-13-01",
                "stats Кино",
                "unknown",
                "help",
                "whoami");

        assertContains(output,
                "Ошибка: Сначала войдите",
                "Ошибка: Неверный логин или пароль",
                "Ошибка: 'abc' не является суммой",
                "Ошибка: Неверная дата '2026-13-01'",
                "Ошибка: Категории не найдены: Кино",
                "Ошибка: Неизвестная команда «unknown»",
                "budget (budgets)",
                "Вы вошли как alice",
                "До свидания!");
        assertFalse(output.contains("Непредвиденная ошибка"));
    }

    private String run(Path dir, String... lines) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        String input = Stream.of(lines).collect(Collectors.joining(System.lineSeparator()));
        FinanceApp.create(dir.resolve("data"), Fixtures.CLOCK, out).run(new BufferedReader(new StringReader(input)));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private void assertContains(String text, String... fragments) {
        Stream.of(fragments).forEach(fragment -> assertTrue(
                text.contains(fragment), () -> "Нет фрагмента «" + fragment + "» в выводе:\n" + text));
    }
}
